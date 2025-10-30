package utn.dds.metamapa.auth;

import com.clerk.backend_api.helpers.security.AuthenticateRequest;
import com.clerk.backend_api.helpers.security.models.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.security.models.RequestState;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler de autenticación para Javalin que valida tokens JWT de Clerk
 *
 * Usa el SDK oficial de Clerk para validar requests.
 * Soporta validación de roles para rutas administrativas.
 */
public class ClerkAuthHandler implements Handler {
    private static final Logger logger = LoggerFactory.getLogger(ClerkAuthHandler.class);

    private final String clerkSecretKey;
    private final boolean requireAdmin;

    /**
     * Constructor que crea un handler de autenticación
     *
     * @param clerkSecretKey La clave secreta de Clerk
     * @param requireAdmin Si es true, solo permite acceso a usuarios con rol "admin"
     */
    public ClerkAuthHandler(String clerkSecretKey, boolean requireAdmin) {
        this.clerkSecretKey = clerkSecretKey;
        this.requireAdmin = requireAdmin;
        logger.info("ClerkAuthHandler inicializado (requireAdmin: {})", requireAdmin);
    }

    /**
     * Constructor que crea un handler que requiere autenticación pero no un rol específico
     *
     * @param clerkSecretKey La clave secreta de Clerk
     */
    public ClerkAuthHandler(String clerkSecretKey) {
        this(clerkSecretKey, false);
    }

    @Override
    public void handle(Context ctx) throws Exception {
        try {
            // Verificar que exista el header Authorization
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || authHeader.trim().isEmpty()) {
                logger.warn("Request sin header Authorization a ruta protegida: {}", ctx.path());
                sendUnauthorizedResponse(ctx, "No autenticado. Se requiere un token válido.");
                return;
            }

            // Verificar si el token ha expirado
            if (isTokenExpired(authHeader)) {
                logger.warn("Token expirado en ruta protegida: {}", ctx.path());
                sendUnauthorizedResponse(ctx, "Token expirado. Por favor, inicie sesión nuevamente.");
                return;
            }

            logger.info("Authorization header presente: {}", authHeader.substring(0, Math.min(20, authHeader.length())) + "...");

            // Convertir el request de Javalin a Map de headers
            Map<String, List<String>> headers = new HashMap<>();
            ctx.headerMap().forEach((key, value) -> headers.put(key, List.of(value)));

            logger.info("Headers enviados a Clerk: {}", headers.keySet());

            // Usar el SDK de Clerk para autenticar el request
            RequestState requestState = AuthenticateRequest.authenticateRequest(
                headers,
                AuthenticateRequestOptions
                    .secretKey(clerkSecretKey)
                    .build()
            );

            logger.info("RequestState - isSignedIn: {}", requestState.isSignedIn());

            // Verificar si el usuario está autenticado
            if (!requestState.isSignedIn()) {
                logger.warn("Request no autenticado a ruta protegida: {}", ctx.path());
                sendUnauthorizedResponse(ctx, "No autenticado. Se requiere un token válido.");
                return;
            }

            // Obtener el userId desde los claims
            String userId = null;
            var claimsOpt = requestState.claims();
            if (claimsOpt.isPresent()) {
                var claims = claimsOpt.get();
                Object sub = claims.get("sub");
                if (sub != null) {
                    userId = sub.toString();
                }
            }

            if (userId == null) {
                logger.warn("Token válido pero sin subject en ruta: {}", ctx.path());
                sendUnauthorizedResponse(ctx, "Token inválido: sin subject");
                return;
            }

            ctx.attribute("userId", userId);
            ctx.attribute("requestState", requestState);

            logger.debug("Usuario autenticado: {} accediendo a: {}", userId, ctx.path());

            // Si se requiere rol de admin, verificar
            if (requireAdmin) {
                String userRole = extractUserRole(requestState);

                if (!"admin".equalsIgnoreCase(userRole)) {
                    logger.warn("Usuario {} con rol '{}' intentó acceder a ruta de admin: {}",
                        userId, userRole, ctx.path());
                    sendForbiddenResponse(ctx, "Acceso denegado. Se requiere rol de administrador.");
                    return;
                }

                logger.debug("Acceso de admin autorizado a: {} por usuario: {}", ctx.path(), userId);
            }

            // Token válido y permisos correctos, continuar con el siguiente handler

        } catch (Exception e) {
            logger.error("Error al validar autenticación en {}: {}", ctx.path(), e.getMessage(), e);
            sendUnauthorizedResponse(ctx, "Error al validar autenticación");
        }
    }

    /**
     * Verifica si el token JWT ha expirado
     * @param token El token JWT (puede incluir "Bearer " al inicio)
     * @return true si el token ha expirado, false en caso contrario
     */
    private boolean isTokenExpired(String token) {
        try {
            // Remover "Bearer " si está presente
            String jwt = token.startsWith("Bearer ") ? token.substring(7) : token;

            // Los JWT tienen 3 partes separadas por puntos: header.payload.signature
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                return false; // No es un JWT válido
            }

            // Decodificar el payload (segunda parte)
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // Buscar el campo "exp" (expiration time)
            // Formato: "exp":1761052205
            int expIndex = payload.indexOf("\"exp\":");
            if (expIndex == -1) {
                return false; // No tiene campo de expiración
            }

            // Extraer el valor numérico después de "exp":
            int startIndex = expIndex + 6; // Longitud de "exp":
            int endIndex = payload.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = payload.indexOf("}", startIndex);
            }

            String expValue = payload.substring(startIndex, endIndex).trim();
            long expirationTime = Long.parseLong(expValue);
            long currentTime = System.currentTimeMillis() / 1000; // Convertir a segundos

            return currentTime > expirationTime;
        } catch (Exception e) {
            logger.warn("Error al verificar expiración del token: {}", e.getMessage());
            return false; // Si hay error, dejamos que Clerk lo valide
        }
    }

    /**
     * Extrae el rol del usuario desde los claims del RequestState
     */
    private String extractUserRole(RequestState requestState) {
        try {
            // Obtener los claims del token (es un Optional)
            var claimsOpt = requestState.claims();

            if (!claimsOpt.isPresent()) {
                return null;
            }

            var claims = claimsOpt.get();

            // Buscar en metadata.role (según configuración de Clerk)
            Object metadata = claims.get("metadata");
            if (metadata instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> metadataMap = (Map<String, Object>) metadata;
                Object role = metadataMap.get("role");
                if (role != null) {
                    return role.toString();
                }
            }

            // También buscar directamente en el claim "role"
            Object directRole = claims.get("role");
            if (directRole != null) {
                return directRole.toString();
            }

            return null;
        } catch (Exception e) {
            logger.warn("Error al extraer rol del usuario: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Envía una respuesta 401 Unauthorized y detiene la ejecución
     */
    private void sendUnauthorizedResponse(Context ctx, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Unauthorized");
        response.put("message", message);
        response.put("status", 401);

        ctx.status(401).json(response).skipRemainingHandlers();
    }

    /**
     * Envía una respuesta 403 Forbidden y detiene la ejecución
     */
    private void sendForbiddenResponse(Context ctx, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Forbidden");
        response.put("message", message);
        response.put("status", 403);

        ctx.status(403).json(response).skipRemainingHandlers();
    }
}

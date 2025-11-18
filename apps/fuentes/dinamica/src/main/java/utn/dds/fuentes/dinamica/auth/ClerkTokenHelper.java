package utn.dds.fuentes.dinamica.auth;

import com.clerk.backend_api.helpers.security.AuthenticateRequest;
import com.clerk.backend_api.helpers.security.models.AuthenticateRequestOptions;
import com.clerk.backend_api.helpers.security.models.RequestState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Helper para extraer información del usuario desde un token de Clerk
 */
public class ClerkTokenHelper {
    private static final Logger logger = LoggerFactory.getLogger(ClerkTokenHelper.class);
    private final String clerkSecretKey;

    public ClerkTokenHelper(String clerkSecretKey) {
        this.clerkSecretKey = clerkSecretKey;
    }

    /**
     * Extrae los datos del usuario desde un token de Clerk
     * @param authHeader El header Authorization con el token (Bearer token)
     * @return Optional con los datos del usuario (userId y nombre) o empty si no se puede extraer
     */
    public Optional<UserInfo> extractUserInfo(String authHeader) {
        if (authHeader == null || authHeader.trim().isEmpty() || clerkSecretKey == null) {
            return Optional.empty();
        }

        try {
            // Convertir el header a un Map de headers para Clerk SDK
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("Authorization", List.of(authHeader));

            // Usar el SDK de Clerk para autenticar el request
            RequestState requestState = AuthenticateRequest.authenticateRequest(
                headers,
                AuthenticateRequestOptions
                    .secretKey(clerkSecretKey)
                    .build()
            );

            if (!requestState.isSignedIn()) {
                logger.warn("Token no válido o usuario no autenticado");
                return Optional.empty();
            }

            // Extraer userId y nombre desde los claims
            String userId = null;
            String nombre = null;

            var claimsOpt = requestState.claims();
            if (claimsOpt.isPresent()) {
                var claims = claimsOpt.get();
                
                // Extraer userId (subject)
                Object sub = claims.get("sub");
                if (sub != null) {
                    userId = sub.toString();
                }

                // Extraer nombre desde los claims
                // Clerk puede tener el nombre en diferentes campos
                Object firstName = claims.get("first_name");
                Object lastName = claims.get("last_name");
                Object fullName = claims.get("name");

                if (fullName != null) {
                    nombre = fullName.toString();
                } else if (firstName != null && lastName != null) {
                    nombre = firstName.toString() + " " + lastName.toString();
                } else if (firstName != null) {
                    nombre = firstName.toString();
                } else if (lastName != null) {
                    nombre = lastName.toString();
                } else {
                    // Intentar obtener desde email si no hay nombre
                    Object email = claims.get("email");
                    if (email != null) {
                        nombre = email.toString();
                    }
                }
            }

            if (userId == null) {
                logger.warn("Token válido pero sin subject (userId)");
                return Optional.empty();
            }

            // Si no hay nombre, usar el userId como fallback
            if (nombre == null || nombre.trim().isEmpty()) {
                nombre = userId;
            }

            return Optional.of(new UserInfo(userId, nombre));

        } catch (Exception e) {
            logger.error("Error al extraer información del usuario desde token de Clerk: {}", e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Clase para almacenar información del usuario extraída del token
     */
    public static class UserInfo {
        private final String userId;
        private final String nombre;

        public UserInfo(String userId, String nombre) {
            this.userId = userId;
            this.nombre = nombre;
        }

        public String getUserId() {
            return userId;
        }

        public String getNombre() {
            return nombre;
        }
    }
}


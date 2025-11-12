package utn.dds.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Handler genérico para AWS Lambda que envuelve una aplicación Javalin.
 * Este handler puede ser reutilizado por todos los servicios que usan Javalin en Lambda.
 * 
 * @param <T> Tipo de la clase Main que contiene el método createApp()
 */
public class JavalinLambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final Logger logger = LoggerFactory.getLogger(JavalinLambdaHandler.class);
    private final Javalin app;
    private final int serverPort;
    private final String pathPrefix;

    /**
     * Constructor que recibe un supplier para crear la app Javalin y el prefijo del path a remover.
     * 
     * @param appSupplier Función que crea la aplicación Javalin
     * @param pathPrefix Prefijo del path a remover (ej: "/estatica", "/dinamica", "/metamapa", "/demo")
     */
    public JavalinLambdaHandler(Supplier<Javalin> appSupplier, String pathPrefix) {
        this.pathPrefix = pathPrefix;
        
        try {
            logger.info("Inicializando Javalin para AWS Lambda");
            this.app = appSupplier.get();

            // Iniciar servidor en puerto local solo para Lambda
            this.app.start(0); // Puerto 0 = puerto aleatorio disponible
            
            // Obtener y almacenar el puerto asignado
            Integer port = this.app.port();
            if (port == null || port == 0) {
                // Si el puerto no está disponible inmediatamente, esperar un poco
                Thread.sleep(100);
                port = this.app.port();
            }
            
            if (port == null || port == 0) {
                throw new RuntimeException("No se pudo obtener el puerto del servidor Javalin");
            }
            
            this.serverPort = port;
            logger.info("Javalin inicializado en puerto: {}", this.serverPort);
        } catch (Exception e) {
            logger.error("Error al inicializar Javalin", e);
            throw new RuntimeException("Error al inicializar Javalin", e);
        }
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        // Log detallado para debugging
        String method = event.getRequestContext().getHttp().getMethod();
        String rawPath = event.getRawPath();
        logger.info("Lambda Request - Method: {}, RawPath: {}, Path: {}", 
            method, 
            rawPath,
            event.getRequestContext().getHttp().getPath());
        
        // Obtener el path del request
        String path = rawPath;
        
        // Si el path es null o vacío, usar "/"
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        
        // Normalizar el path: remover el prefijo si existe
        // (ya que esta Lambda está mapeada a {prefix}/hechos en API Gateway,
        // pero Javalin espera solo /hechos)
        if (pathPrefix != null && !pathPrefix.isEmpty() && path.startsWith(pathPrefix)) {
            path = path.substring(pathPrefix.length());
            if (path.isEmpty()) {
                path = "/";
            }
        }
        
        // Asegurar que el path empiece con /
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        logger.info("Processed path: {}", path);
        
        try {
            // Hacer request HTTP interno a Javalin
            String url = "http://localhost:" + serverPort + path;
            
            // Construir query string desde los query parameters
            if (event.getQueryStringParameters() != null && !event.getQueryStringParameters().isEmpty()) {
                String queryString = event.getQueryStringParameters().entrySet().stream()
                    .map(e -> e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
                url += "?" + queryString;
            }

            logger.info("Forwarding to: {}", url);

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod(method != null ? method : "GET");
            connection.setDoOutput(event.getBody() != null && !event.getBody().isEmpty());

            // Copiar headers
            if (event.getHeaders() != null) {
                event.getHeaders().forEach(connection::setRequestProperty);
            }

            // Enviar body si existe
            if (event.getBody() != null && !event.getBody().isEmpty()) {
                connection.getOutputStream().write(event.getBody().getBytes());
            }

            // Leer respuesta
            int statusCode = connection.getResponseCode();
            String contentType = connection.getContentType();

            java.io.InputStream inputStream = statusCode < 400 ?
                connection.getInputStream() :
                connection.getErrorStream();

            String body = new String(inputStream.readAllBytes());

            return createResponse(statusCode, body, contentType);

        } catch (Exception e) {
            logger.error("Error procesando request en Lambda", e);
            return createErrorResponse(500, "Internal Server Error: " + e.getMessage());
        }
    }

    private APIGatewayV2HTTPResponse createResponse(int statusCode, String body, String contentType) {
        APIGatewayV2HTTPResponse response = new APIGatewayV2HTTPResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType != null ? contentType : "application/json");
        headers.put("Access-Control-Allow-Origin", "*");
        headers.put("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        headers.put("Access-Control-Allow-Headers", "Content-Type");
        response.setHeaders(headers);

        return response;
    }

    private APIGatewayV2HTTPResponse createErrorResponse(int statusCode, String message) {
        return createResponse(statusCode, "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}", "application/json");
    }
}


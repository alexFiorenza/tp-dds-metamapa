package utn.dds.fuentes.estatica;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
    private static final Logger logger = LoggerFactory.getLogger(LambdaHandler.class);
    private static Javalin app;
    private static int serverPort;

    static {
        try {
            logger.info("Inicializando Javalin para AWS Lambda");
            app = Main.createApp();

            // Iniciar servidor en puerto local solo para Lambda
            app.start(0); // Puerto 0 = puerto aleatorio disponible
            
            // Obtener y almacenar el puerto asignado
            Integer port = app.port();
            if (port == null || port == 0) {
                // Si el puerto no está disponible inmediatamente, esperar un poco
                Thread.sleep(100);
                port = app.port();
            }
            
            if (port == null || port == 0) {
                throw new RuntimeException("No se pudo obtener el puerto del servidor Javalin");
            }
            
            serverPort = port;
            logger.info("Javalin inicializado en puerto: {}", serverPort);
        } catch (Exception e) {
            logger.error("Error al inicializar Javalin", e);
            throw new RuntimeException("Error al inicializar Javalin", e);
        }
    }

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest request, Context context) {
        logger.info("Lambda Request - Method: {}, Path: {}", request.getHttpMethod(), request.getPath());

        try {
            // Validar path
            String path = request.getPath();
            if (path == null) {
                path = "/";
            }
            
            // Hacer request HTTP interno a Javalin
            String url = "http://localhost:" + serverPort + path;
            if (request.getQueryStringParameters() != null && !request.getQueryStringParameters().isEmpty()) {
                url += "?" + buildQueryString(request.getQueryStringParameters());
            }

            logger.info("Forwarding to: {}", url);

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod(request.getHttpMethod());
            connection.setDoOutput(request.getBody() != null);

            // Copiar headers
            if (request.getHeaders() != null) {
                request.getHeaders().forEach(connection::setRequestProperty);
            }

            // Enviar body si existe
            if (request.getBody() != null) {
                connection.getOutputStream().write(request.getBody().getBytes());
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

    private String buildQueryString(Map<String, String> params) {
        return params.entrySet().stream()
            .map(e -> e.getKey() + "=" + java.net.URLEncoder.encode(e.getValue(), java.nio.charset.StandardCharsets.UTF_8))
            .reduce((a, b) -> a + "&" + b)
            .orElse("");
    }

    private AwsProxyResponse createResponse(int statusCode, String body, String contentType) {
        AwsProxyResponse response = new AwsProxyResponse();
        response.setStatusCode(statusCode);
        response.setBody(body);

        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", contentType != null ? contentType : "application/json");
        response.setHeaders(headers);

        return response;
    }

    private AwsProxyResponse createErrorResponse(int statusCode, String message) {
        return createResponse(statusCode, "{\"error\": \"" + message.replace("\"", "\\\"") + "\"}", "application/json");
    }
}

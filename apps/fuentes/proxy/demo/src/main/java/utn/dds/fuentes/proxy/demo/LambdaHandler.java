package utn.dds.fuentes.proxy.demo;

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

    static {
        try {
            logger.info("Inicializando Javalin para AWS Lambda");
            app = Main.createApp();
            app.start(0);
            logger.info("Javalin inicializado en puerto: {}", app.port());
        } catch (Exception e) {
            logger.error("Error al inicializar Javalin", e);
            throw new RuntimeException("Error al inicializar Javalin", e);
        }
    }

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest request, Context context) {
        logger.info("Lambda Request - Method: {}, Path: {}", request.getHttpMethod(), request.getPath());

        try {
            String url = "http://localhost:" + app.port() + request.getPath();
            if (request.getQueryStringParameters() != null && !request.getQueryStringParameters().isEmpty()) {
                url += "?" + buildQueryString(request.getQueryStringParameters());
            }

            logger.info("Forwarding to: {}", url);

            java.net.HttpURLConnection connection = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
            connection.setRequestMethod(request.getHttpMethod());
            connection.setDoOutput(request.getBody() != null);

            if (request.getHeaders() != null) {
                request.getHeaders().forEach(connection::setRequestProperty);
            }

            if (request.getBody() != null) {
                connection.getOutputStream().write(request.getBody().getBytes());
            }

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

package utn.dds.scheduler.service;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HttpSchedulerService {
    private static final Logger logger = LoggerFactory.getLogger(HttpSchedulerService.class);
    
    private final OkHttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final String targetUrl;
    private final String httpMethod;
    private final int intervalSeconds;

    public HttpSchedulerService(String targetUrl, int intervalSeconds, String httpMethod) {
        this.targetUrl = targetUrl;
        this.intervalSeconds = intervalSeconds;
        this.httpMethod = httpMethod.toUpperCase();
        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() {
        logger.info("Iniciando scheduler:");
        logger.info("  - URL: {}", targetUrl);
        logger.info("  - Método: {}", httpMethod);
        logger.info("  - Intervalo: {} segundos", intervalSeconds);
        
        scheduler.scheduleAtFixedRate(this::executeRequest, 0, intervalSeconds, TimeUnit.SECONDS);
    }

    private void executeRequest() {
        try {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            logger.info("[{}] Ejecutando {} a {}", timestamp, httpMethod, targetUrl);
            
            Request.Builder requestBuilder = new Request.Builder().url(targetUrl);
            
            Request request = switch (httpMethod) {
                case "GET" -> requestBuilder.get().build();
                case "POST" -> requestBuilder.post(RequestBody.create("", MediaType.get("application/json"))).build();
                case "PUT" -> requestBuilder.put(RequestBody.create("", MediaType.get("application/json"))).build();
                case "DELETE" -> requestBuilder.delete().build();
                default -> throw new IllegalArgumentException("Método HTTP no soportado: " + httpMethod);
            };

            try (Response response = httpClient.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    logger.info("✓ Respuesta exitosa: {} - {}", response.code(), response.message());
                } else {
                    logger.warn("⚠ Respuesta de error: {} - {}", response.code(), response.message());
                }
            }
            
        } catch (IOException e) {
            logger.error("✗ Error en petición HTTP: {}", e.getMessage());
        } catch (Exception e) {
            logger.error("✗ Error inesperado: {}", e.getMessage(), e);
        }
    }

    public void stop() {
        logger.info("Deteniendo scheduler...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        httpClient.dispatcher().executorService().shutdown();
    }
}
package utn.dds.scheduler.config;

public class SchedulerConfig {
    private final String targetUrl;
    private final int intervalSeconds;
    private final String httpMethod;

    public SchedulerConfig(String targetUrl, int intervalSeconds, String httpMethod) {
        this.targetUrl = targetUrl;
        this.intervalSeconds = intervalSeconds;
        this.httpMethod = httpMethod;
    }

    public static SchedulerConfig fromEnvironment() {
        String targetUrl = getEnvOrThrow("TARGET_URL");
        int intervalSeconds = Integer.parseInt(getEnvOrDefault("INTERVAL_SECONDS", "60"));
        String httpMethod = getEnvOrDefault("HTTP_METHOD", "GET");
        
        return new SchedulerConfig(targetUrl, intervalSeconds, httpMethod);
    }

    private static String getEnvOrThrow(String key) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable de entorno requerida: " + key);
        }
        return value;
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null && !value.trim().isEmpty() ? value : defaultValue;
    }

    public String getTargetUrl() {
        return targetUrl;
    }

    public int getIntervalSeconds() {
        return intervalSeconds;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}
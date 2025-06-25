package utn.dds.config;

import java.util.HashMap;
import java.util.Map;

public class DAOConfigBuilder {
    
    public static Map<String, Object> buildFileSystemConfig(String dataUrl) {
        Map<String, Object> config = new HashMap<>();
        config.put("url", dataUrl);
        return config;
    }
    
    public static Map<String, Object> buildS3Config(String dataUrl) {
        // Credenciales requeridas para S3
        String accessKey = System.getenv("S3_ACCESS_KEY");
        String secretKey = System.getenv("S3_SECRET_KEY");
        String bucket = System.getenv("S3_BUCKET");
        String endpoint = System.getenv("S3_ENDPOINT");
        
        if (accessKey == null || secretKey == null || bucket == null || endpoint == null) {
            throw new IllegalArgumentException(
                "Para S3 se requieren las variables: S3_ACCESS_KEY, S3_SECRET_KEY, S3_BUCKET, S3_ENDPOINT"
            );
        }
        
        Map<String, Object> config = new HashMap<>();
        config.put("url", dataUrl);
        config.put("accessKey", accessKey);
        config.put("secretKey", secretKey);
        config.put("bucket", bucket);
        config.put("endpoint", endpoint);
        config.put("region", getEnvOrDefault("S3_REGION", "us-east-1"));
        
        return config;
    }
    
    public static Map<String, Object> buildRedisConfig() {
        Map<String, Object> config = new HashMap<>();
        addIfNotNull(config, "host", getEnvOrDefault("REDIS_HOST", "localhost"));
        addIfNotNull(config, "port", getEnvOrDefault("REDIS_PORT", "6379"));
        addIfNotNull(config, "password", System.getenv("REDIS_PASSWORD"));
        return config;
    }
    
    public static Map<String, Object> buildDAOConfig(String daoType, String dataUrl) {
        switch (daoType.toLowerCase()) {
            case "filesystem":
                return buildFileSystemConfig(dataUrl);
            case "s3":
                return buildS3Config(dataUrl);
            case "redis":
                return buildRedisConfig();
            default:
                throw new IllegalArgumentException("Tipo de DAO no soportado: " + daoType);
        }
    }
    
    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    private static void addIfNotNull(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
    }
}
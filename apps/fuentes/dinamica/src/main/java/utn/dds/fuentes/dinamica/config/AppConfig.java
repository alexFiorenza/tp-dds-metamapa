package utn.dds.fuentes.dinamica.config;

import utn.dds.daos.DAOConfigBuilder;

import java.util.HashMap;
import java.util.Map;

public class AppConfig {
    private final String daoType;
    private final String processorType;
    private final Map<String, Object> daoConfig;
    private final String apiEndpoint;
    private final int refreshIntervalSeconds;

    private AppConfig(String daoType, String processorType, String apiEndpoint, int refreshIntervalSeconds, Map<String, Object> daoConfig) {
        this.daoType = daoType;
        this.processorType = processorType;
        this.daoConfig = daoConfig != null ? daoConfig : new HashMap<>();
        this.apiEndpoint = apiEndpoint;
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    public static AppConfig fromEnvironment() {
        String daoType = getEnvOrDefault("DAO_TYPE", "redis");  // Para dinámicas default Redis
        String processorType = getEnvOrDefault("PROCESSOR_TYPE", "default");
        
        // Configuraciones específicas para fuentes dinámicas
        String apiEndpoint = getEnvOrDefault("API_ENDPOINT", "http://localhost:8080/api/datos");
        int refreshInterval = Integer.parseInt(getEnvOrDefault("REFRESH_INTERVAL_SECONDS", "300")); // 5 min default
        
        // Para dinámicas no necesitamos dataUrl específica, usamos el DAO directamente
        Map<String, Object> daoConfig = DAOConfigBuilder.buildDAOConfig(daoType, null);
        
        return new AppConfig(daoType, processorType, apiEndpoint, refreshInterval, daoConfig);
    }

    // Getters básicos
    public String getDaoType() {
        return daoType;
    }

    public String getProcessorType() {
        return processorType;
    }

    public Map<String, Object> getDaoConfig() {
        return new HashMap<>(daoConfig);
    }

    // Getters específicos para fuentes dinámicas
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public int getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }

    // Utility methods
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
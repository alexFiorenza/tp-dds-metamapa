package utn.dds.fuentes.dinamica.config;

import utn.dds.config.BaseAppConfig;
import utn.dds.config.DAOConfigBuilder;

import java.util.Map;

public class AppConfig extends BaseAppConfig {
    private final String apiEndpoint;
    private final int refreshIntervalSeconds;

    private AppConfig(String daoType, String processorType, String apiEndpoint, int refreshIntervalSeconds, Map<String, Object> daoConfig) {
        super(daoType, processorType, daoConfig);
        this.apiEndpoint = apiEndpoint;
        this.refreshIntervalSeconds = refreshIntervalSeconds;
    }

    public static AppConfig fromEnvironment() {
        String daoType = getEnvOrDefault("DAO_TYPE", "redis");  // Para dinámicas default Redis
        String processorType = getEnvOrDefault("PROCESSOR_TYPE", "json");  // Default JSON para APIs
        
        // Configuraciones específicas para fuentes dinámicas
        String apiEndpoint = getEnvOrDefault("API_ENDPOINT", "http://localhost:8080/api/datos");
        int refreshInterval = Integer.parseInt(getEnvOrDefault("REFRESH_INTERVAL_SECONDS", "300")); // 5 min default
        
        // Para dinámicas no necesitamos dataUrl específica, usamos el DAO directamente
        Map<String, Object> daoConfig = DAOConfigBuilder.buildDAOConfig(daoType, null);
        
        return new AppConfig(daoType, processorType, apiEndpoint, refreshInterval, daoConfig);
    }

    // Getters específicos para fuentes dinámicas
    public String getApiEndpoint() {
        return apiEndpoint;
    }

    public int getRefreshIntervalSeconds() {
        return refreshIntervalSeconds;
    }
}
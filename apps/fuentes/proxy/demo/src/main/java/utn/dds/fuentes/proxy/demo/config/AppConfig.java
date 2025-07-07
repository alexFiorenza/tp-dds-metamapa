package utn.dds.fuentes.proxy.demo.config;

import utn.dds.daos.DAOConfigBuilder;

import java.util.Map;

public class AppConfig {
    private final String daoType;
    private final int tiempoCache;
    private final Map<String, Object> daoConfig;

    private AppConfig(String daoType, int tiempoCache, Map<String, Object> daoConfig) {
        this.daoType = daoType;
        this.tiempoCache = tiempoCache;
        this.daoConfig = daoConfig;
    }

    public static AppConfig fromEnvironment() {
        String daoType = getEnvOrDefault("DAO_TYPE", "none");
        int tiempoCache = Integer.parseInt(getEnvOrDefault("TIEMPO_CACHE", "300")); // 5 minutos por defecto
        
        Map<String, Object> daoConfig = null;
        if (daoType != null && !daoType.trim().isEmpty() && !daoType.equalsIgnoreCase("none")) {
            daoConfig = DAOConfigBuilder.buildDAOConfig(daoType, null);
        }
        
        return new AppConfig(daoType, tiempoCache, daoConfig);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public String getDaoType() {
        return daoType;
    }

    public int getTiempoCache() {
        return tiempoCache;
    }

    public Map<String, Object> getDaoConfig() {
        return daoConfig;
    }
}
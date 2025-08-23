package utn.dds.metamapa.config;

import utn.dds.daos.DAOConfigBuilder;

import java.util.Map;

public class AppConfig {
    private final String daoType;
    private final Map<String, Object> daoConfig;

    private AppConfig(String daoType, Map<String, Object> daoConfig) {
        this.daoType = daoType;
        this.daoConfig = daoConfig;
    }

    public static AppConfig fromEnvironment() {
        String daoType = getEnvOrDefault("DAO_TYPE", "filesystem");
        
        Map<String, Object> daoConfig = null;
        if (daoType != null && !daoType.trim().isEmpty() && !daoType.equalsIgnoreCase("none")) {
            if ("filesystem".equals(daoType)) {
                daoConfig = new java.util.HashMap<>();
            } else {
                daoConfig = DAOConfigBuilder.buildDAOConfig(daoType, null);
            }
        }
        
        return new AppConfig(daoType, daoConfig);
    }

    private static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    public String getDaoType() {
        return daoType;
    }

    public Map<String, Object> getDaoConfig() {
        return daoConfig;
    }
}
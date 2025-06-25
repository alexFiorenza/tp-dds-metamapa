package utn.dds.config;

import java.util.HashMap;
import java.util.Map;

public abstract class BaseAppConfig {
    protected final String daoType;
    protected final String processorType;
    protected final Map<String, Object> daoConfig;

    protected BaseAppConfig(String daoType, String processorType, Map<String, Object> daoConfig) {
        this.daoType = daoType;
        this.processorType = processorType;
        this.daoConfig = daoConfig != null ? daoConfig : new HashMap<>();
    }

    protected static String getEnvOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null ? value : defaultValue;
    }

    protected static void addIfNotNull(Map<String, Object> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value);
        }
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
}
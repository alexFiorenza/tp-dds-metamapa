package utn.dds.fuentes.estatica.config;

import utn.dds.fuentes.estatica.config.BaseAppConfig;
import utn.dds.daos.DAOConfigBuilder;

import java.util.Map;

public class AppConfig extends BaseAppConfig {
    private final String dataUrl;

    private AppConfig(String daoType, String processorType, String dataUrl, Map<String, Object> daoConfig) {
        super(daoType, processorType, daoConfig);
        this.dataUrl = dataUrl;
    }

    public static AppConfig fromEnvironment() {
        String daoType = getEnvOrDefault("DAO_TYPE", "filesystem");
        String processorType = getEnvOrDefault("PROCESSOR_TYPE", "csv");
        String dataUrl = getEnvOrDefault("DATA_URL", "src/main/resources/data/desastres_naturales_argentina.csv");
        
        // Usar el builder compartido para crear la configuración del DAO
        Map<String, Object> daoConfig = DAOConfigBuilder.buildDAOConfig(daoType, dataUrl);
        
        return new AppConfig(daoType, processorType, dataUrl, daoConfig);
    }

    // Getter específico para fuentes estáticas
    public String getDataUrl() {
        return dataUrl;
    }
}
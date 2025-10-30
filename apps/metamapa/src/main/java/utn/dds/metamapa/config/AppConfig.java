package utn.dds.metamapa.config;

import utn.dds.daos.DAOConfigBuilder;

import java.util.Map;

public class AppConfig {
    private final String daoType;
    private final Map<String, Object> daoConfig;
    private final String clerkSecretKey;

    private AppConfig(String daoType, Map<String, Object> daoConfig, String clerkSecretKey) {
        this.daoType = daoType;
        this.daoConfig = daoConfig;
        this.clerkSecretKey = clerkSecretKey;
    }

    public static AppConfig fromEnvironment() {
        String daoType = getEnvOrDefault("DAO_TYPE", "hibernate");
        String clerkSecretKey = System.getenv("CLERK_SECRET_KEY");

        // Validar que CLERK_SECRET_KEY esté configurado
        if (clerkSecretKey == null || clerkSecretKey.trim().isEmpty()) {
            throw new IllegalStateException(
                "CLERK_SECRET_KEY no está configurado. " +
                "Esta variable de entorno es obligatoria para la autenticación. " +
                "Configúrela en el archivo .env o en las variables de entorno del sistema."
            );
        }

        Map<String, Object> daoConfig = null;
        if (daoType != null && !daoType.trim().isEmpty() && !daoType.equalsIgnoreCase("none")) {
            daoConfig = DAOConfigBuilder.buildDAOConfig(daoType, null);
        }

        return new AppConfig(daoType, daoConfig, clerkSecretKey);
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

    public String getClerkSecretKey() {
        return clerkSecretKey;
    }
}
package utn.dds.daos;

import java.util.HashMap;
import java.util.Map;

public class DAOConfigBuilder {
    
    public static Map<String, Object> buildFileSystemConfig(String dataUrl) {
        Map<String, Object> config = new HashMap<>();
        config.put("url", dataUrl);
        return config;
    }
    
    public static Map<String, Object> buildFileSystemConfig() {
        Map<String, Object> config = new HashMap<>();
        // No se requiere URL para FileSystem cuando se usa con path dinámico
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

        // URL pública para acceso externo (opcional, por defecto usa endpoint)
        String publicEndpoint = getEnvOrDefault("S3_PUBLIC_ENDPOINT", endpoint);

        Map<String, Object> config = new HashMap<>();
        config.put("url", dataUrl);
        config.put("accessKey", accessKey);
        config.put("secretKey", secretKey);
        config.put("bucket", bucket);
        config.put("endpoint", endpoint);
        config.put("publicEndpoint", publicEndpoint);
        config.put("region", getEnvOrDefault("S3_REGION", "us-east-1"));

        return config;
    }
    
    public static Map<String, Object> buildS3Config() {
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

        // URL pública para acceso externo (opcional, por defecto usa endpoint)
        String publicEndpoint = getEnvOrDefault("S3_PUBLIC_ENDPOINT", endpoint);

        Map<String, Object> config = new HashMap<>();
        // No se incluye URL para S3 cuando se usa con path dinámico
        config.put("accessKey", accessKey);
        config.put("secretKey", secretKey);
        config.put("bucket", bucket);
        config.put("endpoint", endpoint);
        config.put("publicEndpoint", publicEndpoint);
        config.put("region", getEnvOrDefault("S3_REGION", "us-east-1"));

        return config;
    }

    public static Map<String, Object> buildHibernateConfig() {
        Map<String, Object> config = new HashMap<>();

        // Configuración desde variables de entorno
        config.put("jakarta.persistence.jdbc.url",
            getEnvOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        config.put("jakarta.persistence.jdbc.user",
            getEnvOrDefault("DB_USER", "metamapa"));
        config.put("jakarta.persistence.jdbc.password",
            getEnvOrDefault("DB_PASSWORD", "metamapa123"));
        config.put("persistenceUnit", "metamapa-db");

        return config;
    }

    // Agrego esta configuracion para CouchDB (ver si es correcta)
    public static Map<String, Object> buildCouchDBConfig() {
        Map<String, Object> config = new HashMap<>();

        String baseUrl = getEnvOrDefault("COUCHDB_URL", "http://localhost:5984");
        String dbPrefix = getEnvOrDefault("COUCHDB_DB", "metamapa_db");

        // Guardar URL base y prefijo de DB para que los repositorios lo usen
        config.put("baseUrl", baseUrl);
        config.put("dbPrefix", dbPrefix);
        config.put("username", getEnvOrDefault("COUCHDB_USER", "admin"));
        config.put("password", getEnvOrDefault("COUCHDB_PASSWORD", "admin123"));

        return config;
    }

    public static Map<String, Object> buildMongoDBConfig() {
        Map<String, Object> config = new HashMap<>();

        // Verificar si existe MONGODB_URI (para MongoDB Atlas)
        String mongoUri = System.getenv("MONGODB_URI");

        if (mongoUri != null && !mongoUri.isEmpty()) {
            // MongoDB Atlas: usar URI directamente
            config.put("connectionString", mongoUri);
            config.put("useServerApi", true);
        } else {
            // MongoDB local: construir connection string manualmente
            String mongoHost = getEnvOrDefault("MONGODB_HOST", "localhost");
            String mongoPort = getEnvOrDefault("MONGODB_PORT", "27017");
            String mongoUser = getEnvOrDefault("MONGODB_USER", "admin");
            String mongoPassword = getEnvOrDefault("MONGODB_PASSWORD", "admin123");
            String mongoAuthDb = getEnvOrDefault("MONGODB_AUTH_DB", "admin");

            String connectionString = String.format(
                "mongodb://%s:%s@%s:%s/?authSource=%s",
                mongoUser, mongoPassword, mongoHost, mongoPort, mongoAuthDb
            );

            config.put("connectionString", connectionString);
            config.put("useServerApi", false);
        }

        String mongoDatabase = getEnvOrDefault("MONGODB_DB", "metamapa_db");
        config.put("database", mongoDatabase);
        config.put("dbPrefix", mongoDatabase);

        return config;
    }


    public static Map<String, Object> buildDAOConfig(String daoType, String dataUrl) {
        switch (daoType.toLowerCase()) {
            case "filesystem":
                return buildFileSystemConfig(dataUrl);
            case "s3":
                return buildS3Config(dataUrl);
            case "hibernate":
                return buildHibernateConfig();
            case "couchdb":
                return buildCouchDBConfig();
            case "mongodb":
                return buildMongoDBConfig();
            default:
                throw new IllegalArgumentException("Tipo de DAO no soportado: " + daoType);
        }
    }

    public static Map<String, Object> buildDAOConfig(String daoType) {
        switch (daoType.toLowerCase()) {
            case "filesystem":
                return buildFileSystemConfig();
            case "s3":
                return buildS3Config();
            case "hibernate":
                return buildHibernateConfig();
            case "couchdb":
                return buildCouchDBConfig();
            case "mongodb":
                return buildMongoDBConfig();
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
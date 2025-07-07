package utn.dds.daos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DAOFactory {
    public static <T> IDAO<T> createDAO(Class<T> clazz, String type, Map<String, Object> config) {
        if ("redis".equals(type.toLowerCase()) && !config.containsKey("key")) {
            config.put("key", clazz.getSimpleName().toLowerCase());
        }
        return createDAOInternal(clazz, type, config);
    }
    
    private static <T> IDAO<T> createDAOInternal(Class<T> clazz, String type, Map<String, Object> config) {
        switch (type.toLowerCase()) {
            case "filesystem":
                String url = (String) config.get("url");
                Path path = Paths.get(url);
                return new FileSystem<>(path);
            case "s3":
                String s3Url = (String) config.get("url");
                String accessKey = (String) config.get("accessKey");
                String secretKey = (String) config.get("secretKey");
                String bucket = (String) config.get("bucket");
                String endpoint = (String) config.get("endpoint");
                String region = (String) config.get("region");
                return new S3<>(s3Url, accessKey, secretKey, bucket, endpoint, region);
            case "redis":
                String redisUrl = (String) config.get("url");
                String host = (String) config.getOrDefault("host", "localhost");
                int port = Integer.parseInt(config.getOrDefault("port", "6379").toString());
                String password = (String) config.get("password");
                String key = (String) config.get("key");
                if (key != null) {
                    return new Redis<>(redisUrl, host, port, password, key, clazz);
                } else {
                    return new Redis<>(redisUrl, host, port, password, clazz);
                }
            default:
                throw new IllegalArgumentException("Tipo de DAO no soportado: " + type);
        }
    }
}
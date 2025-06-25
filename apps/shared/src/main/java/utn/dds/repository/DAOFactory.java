package utn.dds.repository;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DAOFactory {
    
    public static <T> IDAO<T> createDAO(String type, Map<String, Object> config) {
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
                return new Redis<>();
            default:
                throw new IllegalArgumentException("Tipo de DAO no soportado: " + type);
        }
    }
}
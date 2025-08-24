package utn.dds.daos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DAOFactory {
    public static <T> IDAO<T> createDAO(Class<T> clazz, String type, Map<String, Object> config) {
        switch (type.toLowerCase()) {
            case "filesystem":
                String url = (String) config.get("url");
                if (url != null) {
                    Path path = Paths.get(url);
                    return new FileSystem<>(path, clazz);
                } else {
                    return new FileSystem<>(clazz);
                }
            case "s3":
                String s3Url = (String) config.get("url");
                String accessKey = (String) config.get("accessKey");
                String secretKey = (String) config.get("secretKey");
                String bucket = (String) config.get("bucket");
                String endpoint = (String) config.get("endpoint");
                String region = (String) config.get("region");
                if (s3Url != null) {
                    return new S3<>(s3Url, accessKey, secretKey, bucket, endpoint, region);
                } else {
                    return new S3<>(accessKey, secretKey, bucket, endpoint, region);
                }
            default:
                throw new IllegalArgumentException("Tipo de DAO no soportado: " + type);
        }
    }
}
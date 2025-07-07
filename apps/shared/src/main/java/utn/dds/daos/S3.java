package utn.dds.daos;

import java.io.InputStream;
import java.util.List;

public class S3<T> implements IDAO<T> {
    private final String url;
    private final String accessKey;
    private final String secretKey;
    private final String bucket;
    private final String endpoint;
    private final String region;

    public S3(String url, String accessKey, String secretKey, String bucket, String endpoint, String region) {
        this.url = url;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.region = region;
    }

    // Constructor backward compatible
    public S3(String url) {
        this(url, null, null, null, null, "us-east-1");
    }

    @Override
    public InputStream read() {
        // TODO: Implementar acceso a S3 con credenciales
        // Aquí usarías las credenciales para conectar a S3/Minio
        return null;
    }

    @Override
    public List<T> find() {
        // TODO: Implementar búsqueda en S3
        return null;
    }

    @Override
    public void save(T object) {
        // TODO: Implementar guardado en S3
    }
    
    @Override
    public void saveAll(List<T> objects) {
        // TODO: Implementar guardado en lote en S3
    }
    
    @Override
    public void addAll(List<T> objects) {
        // TODO: Implementar agregado en lote en S3
    }

    // Getters para debugging/logging
    public String getEndpoint() {
        return endpoint;
    }

    public String getBucket() {
        return bucket;
    }

    public String getRegion() {
        return region;
    }
} 
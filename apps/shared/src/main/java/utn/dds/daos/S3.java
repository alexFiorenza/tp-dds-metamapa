package utn.dds.daos;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

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
        try {
            S3Client s3Client = createS3Client();
            
            String objectKey = extractObjectKeyFromUrl();
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            
            return s3Client.getObject(getObjectRequest);
            
        } catch (S3Exception e) {
            throw new RuntimeException("Error reading object from S3: " + e.getMessage(), e);
        }
    }
    
    private S3Client createS3Client() {
        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(Region.of(region));
        
        if (accessKey != null && secretKey != null) {
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            clientBuilder.credentialsProvider(StaticCredentialsProvider.create(awsCreds));
        }
        
        if (endpoint != null) {
            clientBuilder.endpointOverride(URI.create(endpoint));
        }
        
        return clientBuilder.build();
    }
    
    private String extractObjectKeyFromUrl() {
        if (url == null) {
            throw new IllegalStateException("URL is null");
        }
        
        if (url.startsWith("s3://")) {
            String path = url.substring(5);
            int firstSlash = path.indexOf('/');
            if (firstSlash == -1) {
                throw new IllegalArgumentException("Invalid S3 URL format: " + url);
            }
            return path.substring(firstSlash + 1);
        } else {
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash == -1) {
                return url;
            }
            return url.substring(lastSlash + 1);
        }
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
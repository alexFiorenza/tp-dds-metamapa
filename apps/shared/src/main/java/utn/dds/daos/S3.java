package utn.dds.daos;

import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.S3Exception;

public class S3<T> implements IDAO<T> {
    private static final Logger logger = LoggerFactory.getLogger(S3.class);
    
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
        
        logger.info("Inicializando S3 DAO con configuración:");
        logger.info("  URL: {}", url);
        logger.info("  Endpoint: {}", endpoint);
        logger.info("  Bucket: {}", bucket);
        logger.info("  Region: {}", region);
        logger.info("  AccessKey: {}", accessKey != null ? "***" + accessKey.substring(Math.max(0, accessKey.length() - 4)) : "null");
    }

    // Constructor backward compatible
    public S3(String url) {
        this(url, null, null, null, null, "us-east-1");
    }

    // Constructor without URL for dynamic path usage
    public S3(String accessKey, String secretKey, String bucket, String endpoint, String region) {
        this.url = null;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.endpoint = endpoint;
        this.region = region;
        
        logger.info("Inicializando S3 DAO (sin URL) con configuración:");
        logger.info("  Endpoint: {}", endpoint);
        logger.info("  Bucket: {}", bucket);
        logger.info("  Region: {}", region);
        logger.info("  AccessKey: {}", accessKey != null ? "***" + accessKey.substring(Math.max(0, accessKey.length() - 4)) : "null");
    }

    @Override
    public InputStream read() {
        if (url == null) {
            throw new IllegalStateException("No se puede usar read() sin path cuando S3 fue inicializado sin URL. Use read(path) en su lugar.");
        }
        
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

    @Override
    public InputStream read(String path) {
        try {
            S3Client s3Client = createS3Client();
            
            String objectKey = extractObjectKeyFromPath(path);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(objectKey)
                    .build();
            
            return s3Client.getObject(getObjectRequest);
            
        } catch (S3Exception e) {
            logger.error("Error S3Exception al leer objeto con path {}: {}", path, e.getMessage(), e);
            throw new RuntimeException("Error reading object from S3 with path " + path + ": " + e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Error general al leer objeto con path {}: {}", path, e.getMessage(), e);
            throw new RuntimeException("Error reading object from S3 with path " + path + ": " + e.getMessage(), e);
        }
    }
    
    private S3Client createS3Client() {
        logger.info("Creando S3Client con endpoint: {}, región: {}", endpoint, region);
        
        S3ClientBuilder clientBuilder = S3Client.builder()
                .region(Region.of(region));
        
        if (accessKey != null && secretKey != null) {
            logger.info("Configurando credenciales AWS");
            AwsBasicCredentials awsCreds = AwsBasicCredentials.create(accessKey, secretKey);
            clientBuilder.credentialsProvider(StaticCredentialsProvider.create(awsCreds));
        }
        
        if (endpoint != null) {
            String endpointUrl = endpoint.startsWith("http") ? endpoint : "http://" + endpoint;
            logger.info("Configurando endpoint: {} -> {}", endpoint, endpointUrl);
            
            try {
                URI endpointUri = URI.create(endpointUrl);
                logger.info("URI creado correctamente: {}", endpointUri);
                
                clientBuilder.endpointOverride(endpointUri)
                             .forcePathStyle(true); // Necesario para MinIO
                logger.info("Cliente configurado con forcePathStyle=true");
            } catch (Exception e) {
                logger.error("Error al crear URI del endpoint: {}", e.getMessage(), e);
                throw e;
            }
        }
        
        logger.info("Construyendo cliente S3...");
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

    private String extractObjectKeyFromPath(String path) {
        if (path == null) {
            throw new IllegalArgumentException("Path cannot be null");
        }
        
        if (path.startsWith("s3://")) {
            String pathPart = path.substring(5);
            int firstSlash = pathPart.indexOf('/');
            if (firstSlash == -1) {
                throw new IllegalArgumentException("Invalid S3 URL format: " + path);
            }
            return pathPart.substring(firstSlash + 1);
        } else {
            // For direct object key paths, return as is
            return path;
        }
    }

    /**
     * Sube un archivo binario a S3.
     *
     * @param key           La clave (path) donde se almacenará el archivo en el bucket.
     * @param inputStream   El InputStream del archivo a subir.
     * @param contentLength La longitud del contenido a subir.
     */
    public String uploadBinary(String key, InputStream inputStream, long contentLength) {
        S3Client s3Client = createS3Client();
        try {
            logger.info("Subiendo archivo a S3. Bucket: {}, Key: {}", bucket, key);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            RequestBody requestBody = RequestBody.fromInputStream(inputStream, contentLength);
            s3Client.putObject(putObjectRequest, requestBody);

            String fileUrl = String.format("%s/%s/%s", endpoint, bucket, key);

            logger.info("Archivo subido exitosamente a S3. URL: {}", fileUrl);
            return fileUrl;

        } catch (S3Exception e) {
            logger.error("Error al subir archivo a S3. Key: {}. Error: {}", key, e.getMessage(), e);
            throw new RuntimeException("Error de S3 al subir el archivo: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            logger.error("Error inesperado al subir archivo a S3. Key: {}", key, e);
            throw new RuntimeException("Error inesperado al subir el archivo a S3", e);
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
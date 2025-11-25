package utn.dds.fuentes.dinamica.repositories;

import io.javalin.http.UploadedFile;
import utn.dds.daos.DAOConfigBuilder;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;
import utn.dds.daos.S3;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HechoRepository {
    private final IDAO<HechoDTO> dao;
    private final S3 s3;
    Map<String, Object> s3Config = DAOConfigBuilder.buildS3Config();

    // Constantes para validación de multimedia
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp",
        "video/mp4", "video/mpeg", "video/quicktime", "video/webm"
    );
    private static final Pattern DATA_URI_PATTERN = Pattern.compile("^data:([^;]+);base64,(.+)$");

    {
        s3 = new S3(
                null, // url
                (String) s3Config.get("accessKey"),
                (String) s3Config.get("secretKey"),
                (String) s3Config.get("bucket"),
                (String) s3Config.get("endpoint"),
                (String) s3Config.get("publicEndpoint"),
                (String) s3Config.get("region")
        );
    }

    private static final Logger loggerRepository = LoggerFactory.getLogger(HechoRepository.class);

    public HechoRepository(String daoType, Map<String, Object> daoConfig) {

        if ("filesystem".equals(daoType)) {
            //loggerRepository.info("Estoy dentro de HechoRepository");  -- Logger comentado para debuggeo.
            Map<String, Object> config = new java.util.HashMap<>();
            config.put("url", "src/main/resources/mocks/hechos.json");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, config);
        } else if ("couchdb".equals(daoType)) {
            // Para CouchDB, crear una DB específica para hechos
            Map<String, Object> configHechos = new java.util.HashMap<>(daoConfig);
            String baseUrl = (String) configHechos.get("baseUrl");
            String dbPrefix = (String) configHechos.get("dbPrefix");

            // Crear URL completa: http://localhost:5984/dinamica_db_hechos
            String fullUrl = baseUrl.endsWith("/")
                ? baseUrl + dbPrefix + "_hechos"
                : baseUrl + "/" + dbPrefix + "_hechos";

            configHechos.put("url", fullUrl);
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, configHechos);
        } else if ("mongodb".equals(daoType)) {
            // Para MongoDB, especificar la colección para hechos
            Map<String, Object> configHechos = new java.util.HashMap<>(daoConfig);
            configHechos.put("collection", "hechos");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, configHechos);
        } else {
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, daoConfig);
        }
    }

    public List<Hecho> obtenerHechos() throws IOException {
        //
        List<HechoDTO> hechosDTO = dao.find();
        List<Hecho> hechos = hechosDTO.stream()
                .map(HechoDTO::toHecho)
                .collect(Collectors.toList());
        return hechos;
    }

    public Hecho buscarHecho(String titulo) throws IOException {
        List<Hecho> hechos = obtenerHechos();

        Optional<Hecho> encontrado = hechos.stream()
                .filter(s -> s.getTitulo().equals(titulo))
                .findFirst();

        if (encontrado.isPresent()) {
            Hecho hechoEncontrado = encontrado.get();
            return hechoEncontrado;
        } else {
            throw new NoSuchElementException("No se encontró el Hecho con titulo: " + titulo);
        }
    }

    /**
     * Busca un hecho por su UUID
     */
    public HechoDTO buscarHechoPorUuid(String uuid) throws IOException {
        List<HechoDTO> hechosDTO = dao.find();
        
        Optional<HechoDTO> encontrado = hechosDTO.stream()
                .filter(h -> h.getUuid() != null && h.getUuid().equals(uuid))
                .findFirst();

        if (encontrado.isPresent()) {
            return encontrado.get();
        } else {
            throw new NoSuchElementException("No se encontró el Hecho con UUID: " + uuid);
        }
    }

    /**
     * Actualiza un hecho existente
     */
    public HechoDTO actualizarHecho(HechoDTO hechoActualizado) throws IOException {
        // Actualizar fecha de carga
        hechoActualizado.setFechaCarga(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        dao.save(hechoActualizado);
        return hechoActualizado;
    }

    /**
     * Actualiza un hecho existente con archivos multimedia
     */
    public HechoDTO actualizarHechoConArchivos(HechoDTO hechoActualizado, List<UploadedFile> archivos) throws IOException {
        // Actualizar fecha de carga
        hechoActualizado.setFechaCarga(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (archivos != null && !archivos.isEmpty()) {
            List<String> multimediaUrls = new ArrayList<>();
            int fileIndex = 0;

            for (UploadedFile archivo : archivos) {
                fileIndex++;
                String mimeType = archivo.contentType();
                String extension = archivo.extension();
                long contentLength = archivo.size();

                loggerRepository.info("Procesando archivo #{} para actualización: {} (tipo: {}, tamaño: {} bytes)",
                    fileIndex, archivo.filename(), mimeType, contentLength);

                // Validar tipo MIME
                if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                    loggerRepository.error("Tipo MIME no permitido: {} para archivo #{}", mimeType, fileIndex);
                    throw new IllegalArgumentException(
                        String.format("Tipo MIME no permitido: %s. Tipos permitidos: %s",
                            mimeType, ALLOWED_MIME_TYPES)
                    );
                }

                // Validar tamaño del archivo
                if (contentLength > MAX_FILE_SIZE) {
                    loggerRepository.error("Archivo #{} excede el tamaño máximo: {} bytes (máximo: {} bytes)",
                        fileIndex, contentLength, MAX_FILE_SIZE);
                    throw new IllegalArgumentException(
                        String.format("Archivo #%d excede el tamaño máximo de %d MB (tamaño: %.2f MB)",
                            fileIndex, MAX_FILE_SIZE / (1024 * 1024), contentLength / (1024.0 * 1024.0))
                    );
                }

                // Generar nombre de archivo con extensión
                String ext = extension != null && !extension.isEmpty() ? extension : getExtensionFromMimeType(mimeType);
                String nombreArchivo = UUID.randomUUID().toString() + ext;

                loggerRepository.info("Subiendo archivo #{} para actualización: {}", fileIndex, nombreArchivo);

                // Subir a S3
                try {
                    String url = s3.uploadBinary(nombreArchivo, archivo.content(), contentLength);
                    multimediaUrls.add(url);
                    loggerRepository.info("Archivo #{} subido exitosamente: {}", fileIndex, url);
                } catch (Exception e) {
                    loggerRepository.error("Error subiendo archivo #{} a S3", fileIndex, e);
                    throw new RuntimeException("Error subiendo archivo #" + fileIndex + " a S3: " + e.getMessage(), e);
                }
            }

            // Reemplazar multimedia existente con los nuevos archivos
            hechoActualizado.setMultimedia(multimediaUrls);
        }

        dao.save(hechoActualizado);
        return hechoActualizado;
    }

    // Funcion provisional para persistir los hechos
    public void saveProvisional(HechoDTO hechoDto) {
        if (hechoDto.getMultimedia() != null && !hechoDto.getMultimedia().isEmpty()) {
            List<String> multimediaUrls = new ArrayList<>();
            int fileIndex = 0;

            for (String contenidoBase64 : hechoDto.getMultimedia()) {
                fileIndex++;
                String mimeType = null;
                String base64Data = contenidoBase64;

                // Extraer MIME type y datos Base64 si viene con prefijo data URI
                Matcher matcher = DATA_URI_PATTERN.matcher(contenidoBase64);
                if (matcher.matches()) {
                    mimeType = matcher.group(1);
                    base64Data = matcher.group(2);

                    // Validar tipo MIME
                    if (!ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                        loggerRepository.error("Tipo MIME no permitido: {} para archivo #{}", mimeType, fileIndex);
                        throw new IllegalArgumentException(
                            String.format("Tipo MIME no permitido: %s. Tipos permitidos: %s",
                                mimeType, ALLOWED_MIME_TYPES)
                        );
                    }
                } else if (contenidoBase64.contains(",")) {
                    // Formato legacy: solo quitar el prefijo sin validar MIME
                    base64Data = contenidoBase64.substring(contenidoBase64.indexOf(',') + 1);
                    loggerRepository.warn("Archivo #{} sin MIME type especificado, se permite por compatibilidad", fileIndex);
                }

                // Decodificar Base64
                byte[] datosBinarios;
                try {
                    datosBinarios = Base64.getDecoder().decode(base64Data);
                } catch (IllegalArgumentException e) {
                    loggerRepository.error("Error al decodificar Base64 para archivo #{}", fileIndex, e);
                    throw new IllegalArgumentException("Formato Base64 inválido para archivo #" + fileIndex, e);
                }

                // Validar tamaño del archivo
                long contentLength = datosBinarios.length;
                if (contentLength > MAX_FILE_SIZE) {
                    loggerRepository.error("Archivo #{} excede el tamaño máximo: {} bytes (máximo: {} bytes)",
                        fileIndex, contentLength, MAX_FILE_SIZE);
                    throw new IllegalArgumentException(
                        String.format("Archivo #%d excede el tamaño máximo de %d MB (tamaño: %.2f MB)",
                            fileIndex, MAX_FILE_SIZE / (1024 * 1024), contentLength / (1024.0 * 1024.0))
                    );
                }

                // Generar nombre de archivo con extensión basada en MIME type
                String extension = getExtensionFromMimeType(mimeType);
                String nombreArchivo = UUID.randomUUID().toString() + extension;

                loggerRepository.info("Subiendo archivo #{}: {} (tipo: {}, tamaño: {} bytes)",
                    fileIndex, nombreArchivo, mimeType != null ? mimeType : "desconocido", contentLength);

                // Subir a S3
                try (ByteArrayInputStream bais = new ByteArrayInputStream(datosBinarios)) {
                    String url = s3.uploadBinary(nombreArchivo, bais, contentLength);
                    multimediaUrls.add(url);
                    loggerRepository.info("Archivo #{} subido exitosamente: {}", fileIndex, url);
                } catch (Exception e) {
                    loggerRepository.error("Error subiendo archivo #{} a S3", fileIndex, e);
                    throw new RuntimeException("Error subiendo archivo #" + fileIndex + " a S3: " + e.getMessage(), e);
                }
            }
            hechoDto.setMultimedia(multimediaUrls);
        }
        loggerRepository.info("saveProvisional - Contribuyente antes de dao.save: {}", 
            hechoDto.getContribuyente() != null ? 
                "nombre=" + hechoDto.getContribuyente().getNombre() + ", userId=" + hechoDto.getContribuyente().getUserId() : 
                "null");
        dao.save(hechoDto);
        loggerRepository.info("saveProvisional - Hecho guardado en CouchDB");
    }

    /**
     * Obtiene la extensión de archivo apropiada basada en el tipo MIME
     */
    private String getExtensionFromMimeType(String mimeType) {
        if (mimeType == null) {
            return "";
        }

        return switch (mimeType.toLowerCase()) {
            case "image/jpeg", "image/jpg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/gif" -> ".gif";
            case "image/webp" -> ".webp";
            case "video/mp4" -> ".mp4";
            case "video/mpeg" -> ".mpeg";
            case "video/quicktime" -> ".mov";
            case "video/webm" -> ".webm";
            default -> "";
        };
    }

    public HechoDTO aportarHecho(HechoDTO hecho) throws IOException {
        loggerRepository.info("aportarHecho - Contribuyente antes de guardar: {}",
            hecho.getContribuyente() != null ?
                "nombre=" + hecho.getContribuyente().getNombre() + ", userId=" + hecho.getContribuyente().getUserId() :
                "null");

        // Generar UUID si no tiene uno
        if (hecho.getUuid() == null || hecho.getUuid().trim().isEmpty()) {
            hecho.setUuid(UUID.randomUUID().toString());
            loggerRepository.info("UUID generado para el hecho: {}", hecho.getUuid());
        }

        // Asignar fecha de carga actual automáticamente en formato ISO-8601
        hecho.setFechaCarga(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        this.saveProvisional(hecho);   // Funcion provisional para guardar los hechos en couchdb con multimedia en minio s3
        loggerRepository.info("aportarHecho - Hecho guardado con UUID: {}", hecho.getUuid());
        return hecho;
    }

    /**
     * Aporta un hecho con archivos multimedia subidos directamente como binarios (multipart/form-data).
     * Los archivos se procesan y suben a S3, reemplazando el contenido por URLs.
     */
    public HechoDTO aportarHechoConArchivos(HechoDTO hecho, List<UploadedFile> archivos) throws IOException {
        // Generar UUID si no tiene uno
        if (hecho.getUuid() == null || hecho.getUuid().trim().isEmpty()) {
            hecho.setUuid(UUID.randomUUID().toString());
            loggerRepository.info("UUID generado para el hecho: {}", hecho.getUuid());
        }

        // Asignar fecha de carga actual automáticamente en formato ISO-8601
        hecho.setFechaCarga(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        if (archivos != null && !archivos.isEmpty()) {
            List<String> multimediaUrls = new ArrayList<>();
            int fileIndex = 0;

            for (UploadedFile archivo : archivos) {
                fileIndex++;
                String mimeType = archivo.contentType();
                String extension = archivo.extension();
                long contentLength = archivo.size();

                loggerRepository.info("Procesando archivo #{}: {} (tipo: {}, tamaño: {} bytes)",
                    fileIndex, archivo.filename(), mimeType, contentLength);

                // Validar tipo MIME
                if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                    loggerRepository.error("Tipo MIME no permitido: {} para archivo #{}", mimeType, fileIndex);
                    throw new IllegalArgumentException(
                        String.format("Tipo MIME no permitido: %s. Tipos permitidos: %s",
                            mimeType, ALLOWED_MIME_TYPES)
                    );
                }

                // Validar tamaño del archivo
                if (contentLength > MAX_FILE_SIZE) {
                    loggerRepository.error("Archivo #{} excede el tamaño máximo: {} bytes (máximo: {} bytes)",
                        fileIndex, contentLength, MAX_FILE_SIZE);
                    throw new IllegalArgumentException(
                        String.format("Archivo #%d excede el tamaño máximo de %d MB (tamaño: %.2f MB)",
                            fileIndex, MAX_FILE_SIZE / (1024 * 1024), contentLength / (1024.0 * 1024.0))
                    );
                }

                // Generar nombre de archivo con extensión
                String ext = extension != null && !extension.isEmpty() ? extension : getExtensionFromMimeType(mimeType);
                String nombreArchivo = UUID.randomUUID().toString() + ext;

                loggerRepository.info("Subiendo archivo #{}: {}", fileIndex, nombreArchivo);

                // Subir a S3
                try {
                    String url = s3.uploadBinary(nombreArchivo, archivo.content(), contentLength);
                    multimediaUrls.add(url);
                    loggerRepository.info("Archivo #{} subido exitosamente: {}", fileIndex, url);
                } catch (Exception e) {
                    loggerRepository.error("Error subiendo archivo #{} a S3", fileIndex, e);
                    throw new RuntimeException("Error subiendo archivo #" + fileIndex + " a S3: " + e.getMessage(), e);
                }
            }

            hecho.setMultimedia(multimediaUrls);
        }

        dao.save(hecho);
        return hecho;
    }

    public Hecho cambiarEstado(Hecho hecho) throws IOException {
        if(hecho.getEstado()== EstadoHecho.ACTIVO){
            hecho.ocultar();
        } else { hecho.activar(); }
        return hecho;
    }
} 

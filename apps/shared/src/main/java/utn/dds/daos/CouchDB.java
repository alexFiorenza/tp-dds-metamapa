package utn.dds.daos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;

/**
 * DAO genérico para CouchDB.
 * Permite conectar, leer y guardar documentos en una base CouchDB.
 */
public class CouchDB<T> implements IDAO<T> {
    private static final Logger logger = LoggerFactory.getLogger(CouchDB.class);

    private final String databaseUrl;
    private final String username;
    private final String password;
    private final Gson gson;
    private final Class<T> type;

    public CouchDB(String databaseUrl, String username, String password, Class<T> type) {
        this.databaseUrl = databaseUrl.endsWith("/") ? databaseUrl : databaseUrl + "/";
        this.username = username;
        this.password = password;
        this.gson = new Gson();
        this.type = type;

        logger.info("Inicializando CouchDB DAO con configuración:");
        logger.info("  URL base: {}", this.databaseUrl);
        logger.info("  Usuario: {}", username != null ? username : "anónimo");

        // Intentar crear la base de datos si no existe
        ensureDatabaseExists();
    }

    /**
     * Verifica que la base de datos exista, y si no existe, la crea automáticamente.
     */
    private void ensureDatabaseExists() {
        try {
            // Intentar hacer un GET a la base de datos para verificar si existe
            logger.info("Verificando si la base de datos existe...");
            request("GET", "", null);
            logger.info("Base de datos existe correctamente");
        } catch (RuntimeException e) {
            // Verificar si es un error 404 (base de datos no existe)
            boolean is404 = e.getCause() != null && e.getCause().getMessage() != null &&
                           e.getCause().getMessage().contains("404");

            if (is404) {
                logger.warn("Base de datos no existe. Intentando crearla...");
                try {
                    // Crear la base de datos con un PUT request
                    String response = request("PUT", "", null);
                    logger.info("Base de datos creada exitosamente: {}", response);
                } catch (RuntimeException createError) {
                    logger.error("Error al crear la base de datos: {}", createError.getMessage());
                    throw new RuntimeException("No se pudo crear la base de datos en CouchDB", createError);
                }
            } else {
                // Si es otro error (ej: Connection refused), lo propagamos
                throw e;
            }
        }
    }

    // Constructor sin credenciales (por ejemplo para acceso local sin auth)
    public CouchDB(String databaseUrl, Class<T> type) {
        this(databaseUrl, null, null, type);
    }

    /**
     * Realiza una solicitud HTTP a CouchDB.
     */
    private String request(String method, String endpoint, String body) {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(databaseUrl + endpoint);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // Autenticación básica si hay usuario y contraseña
            if (username != null && password != null) {
                String basicAuth = java.util.Base64.getEncoder()
                        .encodeToString((username + ":" + password).getBytes());
                connection.setRequestProperty("Authorization", "Basic " + basicAuth);
            }

            if (body != null) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body.getBytes(StandardCharsets.UTF_8));
                }
            }

            int responseCode = connection.getResponseCode();
            logger.info("HTTP {} -> {} {}", method, url, responseCode);

            InputStream inputStream = (responseCode >= 200 && responseCode < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            try (Scanner scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
                String responseBody = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";
                if (responseCode >= 300) {
                    throw new IOException("Respuesta HTTP " + responseCode + ": " + responseBody);
                }
                return responseBody;
            }

        } catch (IOException e) {
            logger.error("Error en la solicitud HTTP {} a CouchDB: {}", method, e.getMessage(), e);
            throw new RuntimeException("Error comunicándose con CouchDB", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    /**
     * Obtiene todos los documentos de la base.
     */
    @Override
    public List<T> find() {
        try {
            logger.info("Buscando todos los documentos en CouchDB...");
            String response = request("GET", "_all_docs?include_docs=true", null);

            // El tipo de respuesta de CouchDB para _all_docs es un objeto que contiene 'rows'
            Type responseType = TypeToken.getParameterized(CouchDbAllDocsResponse.class, type).getType();
            CouchDbAllDocsResponse<T> allDocsResponse = gson.fromJson(response, responseType);

            if (allDocsResponse == null || allDocsResponse.rows == null) {
                return java.util.Collections.emptyList();
            }

            // Extraemos el documento de cada fila
            return allDocsResponse.rows.stream()
                    .map(row -> row.doc)
                    .collect(java.util.stream.Collectors.toList());

        } catch (Exception e) {
            logger.error("Error al buscar documentos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener documentos de CouchDB", e);
        }
    }

    /**
     * Guarda un documento individual en CouchDB.
     */
    @Override
    public void save(T object) {
        try {
            logger.info("Guardando documento en CouchDB...");
            String json = gson.toJson(object);
            String response = request("POST", "", json);
            logger.info("Documento guardado correctamente: {}", response);
        } catch (Exception e) {
            logger.error("Error al guardar documento: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar documento en CouchDB", e);
        }
    }

    /**
     * Guarda múltiples documentos en una sola operación de bulk insert.
     */
    @Override
    public void saveAll(List<T> objects) {
        try {
            logger.info("Guardando múltiples documentos (bulk insert)...");
            String json = gson.toJson(new BulkDocs<>(objects));
            String response = request("POST", "_bulk_docs", json);
            logger.info("Bulk insert exitoso: {}", response);
        } catch (Exception e) {
            logger.error("Error en bulk insert: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar documentos en lote", e);
        }
    }

    @Override
    public void addAll(List<T> objects) {
        saveAll(objects);
    }

    // Clases auxiliares para parsear la respuesta de _all_docs
    private static class CouchDbAllDocsResponse<T> {
        List<Row<T>> rows;
    }

    private static class Row<T> {
        T doc;
    }

    // Clase auxiliar para bulk insert en CouchDB
    private static class BulkDocs<T> {
        final List<T> docs;
        BulkDocs(List<T> docs) {
            this.docs = docs;
        }
    }


    /*
     * Estas operaciones no son soportadas, estan para que compile el codigo
     */
    @Override
    public InputStream read() {
        throw new UnsupportedOperationException("CouchDB no soporta operaciones de InputStream. Use find() para obtener entidades.");
    }

    @Override
    public InputStream read(String path) {
        throw new UnsupportedOperationException("CouchDB no soporta operaciones de InputStream. Use find() para obtener entidades.");
    }
}

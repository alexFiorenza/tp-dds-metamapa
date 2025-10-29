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

    // Me pide que tenga esto para que compile, sino tira error (preguntar)
    @Override
    public InputStream read() {
        try {
            logger.info("Leyendo todos los documentos de CouchDB como InputStream...");
            String response = request("GET", "_all_docs?include_docs=true", null);
            return new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Error al leer documentos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al leer documentos de CouchDB", e);
        }
    }

    @Override
    public InputStream read(String path) {
        try {
            logger.info("Leyendo todos los documentos de CouchDB como InputStream...");
            String response = request("GET", "_all_docs?include_docs=true", null);
            return new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            logger.error("Error al leer documentos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al leer documentos de CouchDB", e);
        }
    }
    // Hasta aca deberia borrar porque no lo necesito

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
}

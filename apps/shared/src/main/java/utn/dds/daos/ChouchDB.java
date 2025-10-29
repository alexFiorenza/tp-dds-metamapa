package utn.dds.daos;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
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
                    os.write(body.getBytes());
                }
            }

            int responseCode = connection.getResponseCode();
            logger.info("HTTP {} -> {} {}", method, url, responseCode);

            Scanner scanner = new Scanner(connection.getInputStream());
            String responseBody = scanner.useDelimiter("\\A").next();
            scanner.close();

            return responseBody;

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

            Type listType = TypeToken.getParameterized(List.class, type).getType();
            // CouchDB devuelve una estructura más compleja; aquí simplificamos el parseo
            return gson.fromJson(response, listType);

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

    // Clase auxiliar para bulk insert en CouchDB
    private static class BulkDocs<T> {
        final List<T> docs;
        BulkDocs(List<T> docs) {
            this.docs = docs;
        }
    }
}

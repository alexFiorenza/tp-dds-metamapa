package utn.dds.daos;

import com.google.gson.Gson;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

/**
 * DAO genérico para MongoDB.
 * Permite conectar, leer y guardar documentos en una base MongoDB.
 */
public class MongoDB<T> implements IDAO<T> {
    private static final Logger logger = LoggerFactory.getLogger(MongoDB.class);

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;
    private final Class<T> type;
    private final Gson gson;

    public MongoDB(String connectionString, String databaseName, String collectionName, Class<T> type) {
        this.type = type;
        this.gson = new Gson();

        logger.info("Inicializando MongoDB DAO con configuración:");
        logger.info("  Connection String: {}", maskConnectionString(connectionString));
        logger.info("  Database: {}", databaseName);
        logger.info("  Collection: {}", collectionName);

        try {
            // Configurar codec para POJO
            CodecRegistry pojoCodecRegistry = fromRegistries(
                MongoClientSettings.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build())
            );

            // Crear builder de settings
            MongoClientSettings.Builder settingsBuilder = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(connectionString))
                .codecRegistry(pojoCodecRegistry);

            // Si es MongoDB Atlas (mongodb+srv), agregar Server API V1
            if (connectionString.contains("mongodb+srv")) {
                logger.info("Detectado MongoDB Atlas, usando Server API V1");
                ServerApi serverApi = ServerApi.builder()
                    .version(ServerApiVersion.V1)
                    .build();
                settingsBuilder.serverApi(serverApi);
            }

            MongoClientSettings settings = settingsBuilder.build();
            this.mongoClient = MongoClients.create(settings);
            this.database = mongoClient.getDatabase(databaseName);
            this.collection = database.getCollection(collectionName);

            logger.info("Conexión a MongoDB establecida correctamente");

            // Verificar conexión
            database.listCollectionNames().first();
            logger.info("Colección '{}' lista para uso", collectionName);

        } catch (Exception e) {
            logger.error("Error al conectar con MongoDB: {}", e.getMessage(), e);
            throw new RuntimeException("No se pudo conectar con MongoDB", e);
        }
    }

    /**
     * Enmascara la contraseña en el connection string para logging seguro
     */
    private String maskConnectionString(String connectionString) {
        if (connectionString == null) return "null";
        return connectionString.replaceAll("://([^:]+):([^@]+)@", "://$1:****@");
    }

    /**
     * Obtiene todos los documentos de la colección.
     */
    @Override
    public List<T> find() {
        try {
            logger.info("Buscando todos los documentos en MongoDB...");
            List<T> results = new ArrayList<>();

            collection.find().forEach(doc -> {
                // Remover _id de MongoDB antes de deserializar (si no es parte del DTO)
                doc.remove("_id");
                T object = gson.fromJson(doc.toJson(), type);
                results.add(object);
            });

            logger.info("Se encontraron {} documentos", results.size());
            return results;

        } catch (Exception e) {
            logger.error("Error al buscar documentos: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener documentos de MongoDB", e);
        }
    }

    /**
     * Guarda un documento individual en MongoDB.
     */
    @Override
    public void save(T object) {
        try {
            logger.info("Guardando documento en MongoDB...");
            String json = gson.toJson(object);
            Document doc = Document.parse(json);

            collection.insertOne(doc);
            logger.info("Documento guardado correctamente");

        } catch (Exception e) {
            logger.error("Error al guardar documento: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar documento en MongoDB", e);
        }
    }

    /**
     * Guarda múltiples documentos en una sola operación de bulk insert.
     */
    @Override
    public void saveAll(List<T> objects) {
        try {
            logger.info("Guardando múltiples documentos (bulk insert)...");

            if (objects == null || objects.isEmpty()) {
                logger.warn("Lista de objetos vacía, no se guardará nada");
                return;
            }

            List<Document> documents = new ArrayList<>();
            for (T object : objects) {
                String json = gson.toJson(object);
                Document doc = Document.parse(json);
                documents.add(doc);
            }

            collection.insertMany(documents);
            logger.info("Bulk insert exitoso: {} documentos guardados", objects.size());

        } catch (Exception e) {
            logger.error("Error en bulk insert: {}", e.getMessage(), e);
            throw new RuntimeException("Error al guardar documentos en lote", e);
        }
    }

    @Override
    public void addAll(List<T> objects) {
        saveAll(objects);
    }

    /**
     * Cierra la conexión con MongoDB.
     * Debe llamarse cuando ya no se necesite el DAO.
     */
    public void close() {
        if (mongoClient != null) {
            logger.info("Cerrando conexión con MongoDB...");
            mongoClient.close();
        }
    }

    /*
     * Estas operaciones no son soportadas, están para que compile el código
     */
    @Override
    public InputStream read() {
        throw new UnsupportedOperationException("MongoDB no soporta operaciones de InputStream. Use find() para obtener entidades.");
    }

    @Override
    public InputStream read(String path) {
        throw new UnsupportedOperationException("MongoDB no soporta operaciones de InputStream. Use find() para obtener entidades.");
    }
}

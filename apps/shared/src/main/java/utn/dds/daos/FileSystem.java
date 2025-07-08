package utn.dds.daos;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class FileSystem<T> implements IDAO<T> {
    private Path url;
    private ObjectMapper objectMapper;
    private Class<T> clazz;

    public FileSystem(Path url, Class<T> clazz) {
        this.url = url;
        this.clazz = clazz;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public InputStream read() {
        try {
            // Intentar leer desde el classpath primero
            String relativePath = url.toString();
            
            // Si la ruta contiene src/main/resources/, quitarla para el classpath
            if (relativePath.startsWith("src/main/resources/")) {
                relativePath = relativePath.substring("src/main/resources/".length());
            }
            
            InputStream classPathStream = getClass().getClassLoader().getResourceAsStream(relativePath);
            if (classPathStream != null) {
                return classPathStream;
            }
            
            // Si no se encuentra en el classpath, intentar como archivo físico
            return java.nio.file.Files.newInputStream(url);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al leer archivo: " + url, e);
        }
    }

    @Override
    public List<T> find() {
        try {
            InputStream inputStream = read();
            if (inputStream != null) {
                return objectMapper.readValue(inputStream, 
                    objectMapper.getTypeFactory().constructCollectionType(List.class, clazz));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar lista desde archivo: " + url, e);
        }
        return List.of();
    }

    @Override
    public void save(T object) {
        try {
            List<T> objectList = List.of(object);
            
            // Crear directorio padre si no existe
            java.io.File file = url.toFile();
            java.io.File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            objectMapper.writeValue(file, objectList);
        } catch (Exception e) {
            throw new RuntimeException("Error al guardar objeto en archivo: " + url, e);
        }
    }
    
    @Override
    public void saveAll(List<T> objects) {
        // TODO: Implementar guardado en lote en FileSystem
    }
    
    @Override
    public void addAll(List<T> objects) {
        // TODO: Implementar agregado en lote en FileSystem
    }
} 
package utn.dds.repository;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class FileSystem<T> implements IDAO<T> {
    private Path url;

    public FileSystem(Path url) {
        this.url = url;
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
        // TODO: Implementar búsqueda en FileSystem
        return null;
    }

    @Override
    public void save(T object) {
        // TODO: Implementar guardado en FileSystem
    }
} 
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
        // TODO: Implementar acceso a FileSystem
        return null;
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
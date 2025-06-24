package utn.dds.repository;

import java.io.InputStream;
import java.util.List;

public class S3<T> implements IDAO<T> {
    private String url;

    public S3(String url) {
        this.url = url;
    }

    @Override
    public InputStream read() {
        // TODO: Implementar acceso a S3
        return null;
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
} 
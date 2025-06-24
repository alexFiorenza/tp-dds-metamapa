package utn.dds.repository;

import java.io.InputStream;
import java.util.List;

public class Redis<T> implements IDAO<T> {
    @Override
    public InputStream read() {
        // TODO: Implementar acceso a Redis
        return null;
    }

    @Override
    public List<T> find() {
        // TODO: Implementar búsqueda en Redis
        return null;
    }

    @Override
    public void save(T object) {
        // TODO: Implementar guardado en Redis
    }
} 
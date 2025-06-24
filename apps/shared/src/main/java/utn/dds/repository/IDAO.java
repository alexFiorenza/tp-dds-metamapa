package utn.dds.repository;

import java.io.InputStream;
import java.util.List;

public interface IDAO<T> {
    InputStream read();
    List<T> find();
    void save(T object);
} 
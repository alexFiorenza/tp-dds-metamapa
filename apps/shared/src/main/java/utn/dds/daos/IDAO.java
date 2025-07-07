package utn.dds.daos;

import java.io.InputStream;
import java.util.List;

public interface IDAO<T> {
    InputStream read();
    List<T> find();
    void save(T object);
    void saveAll(List<T> objects);
    void addAll(List<T> objects);
} 
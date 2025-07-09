package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.dominio.Coleccion;

import java.util.List;
import java.util.Map;

public class ColeccionRepository {
    private final IDAO<Coleccion> dao;

    public ColeccionRepository(String daoType, Map<String, Object> daoConfig) {
        this.dao = DAOFactory.createDAO(Coleccion.class, daoType, daoConfig);
    }

    public List<Coleccion> obtenerTodas() {
        return this.dao.find();
    }

    public Coleccion obtenerPorId(String id) {
        List<Coleccion> colecciones = this.dao.find();
        return colecciones.stream()
            .filter(c -> c.getTitulo().equals(id))
            .findFirst()
            .orElse(null);
    }

    public void crear(Coleccion coleccion) {
        this.dao.save(coleccion);
    }

    public void actualizar(String id, Coleccion coleccionActualizada) {
        List<Coleccion> colecciones = this.dao.find();
        for (int i = 0; i < colecciones.size(); i++) {
            if (colecciones.get(i).getTitulo().equals(id)) {
                colecciones.set(i, coleccionActualizada);
                break;
            }
        }
        this.dao.saveAll(colecciones);
    }

    public void eliminar(String id) {
        List<Coleccion> colecciones = this.dao.find();
        colecciones.removeIf(c -> c.getTitulo().equals(id));
        this.dao.saveAll(colecciones);
    }
}
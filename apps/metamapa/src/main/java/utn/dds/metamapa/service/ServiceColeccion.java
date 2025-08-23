package utn.dds.metamapa.service;

import utn.dds.metamapa.persistencia.ColeccionRepository;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;

import java.util.List;
import java.util.Map;

public class ServiceColeccion {
    private final ColeccionRepository coleccionRepository;

    public ServiceColeccion(String daoType, Map<String, Object> daoConfig) {
        this.coleccionRepository = new ColeccionRepository(daoType, daoConfig);
    }

    public List<Coleccion> obtenerColecciones() {
        return this.coleccionRepository.obtenerTodas();
    }

    public Coleccion obtenerColeccionPorId(String id) {
        return this.coleccionRepository.obtenerPorId(id);
    }

    public void crearColeccion(Coleccion coleccion) {
        this.coleccionRepository.crear(coleccion);
    }

    public void actualizarColeccion(String id, Coleccion coleccionActualizada) {
        Coleccion coleccionExistente = this.coleccionRepository.obtenerPorId(id);
        if (coleccionExistente == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        this.coleccionRepository.actualizar(id, coleccionActualizada);
    }

    public void eliminarColeccion(String id) {
        Coleccion coleccionExistente = this.coleccionRepository.obtenerPorId(id);
        if (coleccionExistente == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        this.coleccionRepository.eliminar(id);
    }

    public List<Hecho> buscarHechosEnColeccion(String id, List<HechoStrategy> filtros) {
        Coleccion coleccion = this.coleccionRepository.obtenerPorId(id);
        if (coleccion == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        return coleccion.buscarHechos(filtros);
    }
}
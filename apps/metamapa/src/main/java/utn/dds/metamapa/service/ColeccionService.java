package utn.dds.metamapa.service;

import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.metamapa.persistencia.ColeccionRepository;

import java.util.List;
import java.util.Map;

public class ColeccionService {
    private final ColeccionRepository coleccionRepository;

    public ColeccionService(Map<String, Object> daoConfig) {
        this.coleccionRepository = new ColeccionRepository(daoConfig);
    }

    public List<Coleccion> obtenerTodasLasColecciones() {
        return coleccionRepository.obtenerTodas();
    }

    public Coleccion obtenerColeccionPorId(String handle) {
        return coleccionRepository.obtenerPorId(handle);
    }

    public List<Hecho> obtenerHechosDeColeccion(String handle) {
        // Este método automáticamente aplica los criterios de pertenencia
        return coleccionRepository.obtenerHechos(handle);
    }

    public void crearColeccion(Coleccion coleccion) {
        coleccionRepository.crear(coleccion);
    }

    public void actualizarColeccion(String handle, Coleccion coleccionActualizada) {
        coleccionRepository.actualizar(handle, coleccionActualizada);
    }

    public void eliminarColeccion(String handle) {
        coleccionRepository.eliminar(handle);
    }

    public void close() {
        coleccionRepository.close();
    }
}
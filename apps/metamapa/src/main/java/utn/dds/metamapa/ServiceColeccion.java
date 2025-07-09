package utn.dds.metamapa;

import utn.dds.dominio.Hecho;

import java.util.Collection;
import java.util.List;

public class ServiceColeccion {
    private final RepositoryColeccion coleccionRepository;

    public ServiceColeccion() {
        this.coleccionRepository = new RepositoryColeccion();
    }

    public List<Hecho> buscarColeccion(String coleccionId, String categoria,String fechaReporteDesde, String fechaReporteHasta, String fechaAcontecimientoDesde,String fechaAcontecimientoHasta ) {
        Collection coleccion=coleccionRepository.findById();

        return coleccionDTO;//TODO debe cambiarse a lista de hechos que cumplan los paramteros
    }
}

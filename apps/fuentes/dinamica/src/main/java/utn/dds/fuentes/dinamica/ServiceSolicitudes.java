package utn.dds.fuentes.dinamica;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;

import java.io.IOException;
import java.util.List;

public class ServiceSolicitudes {
    private final DinamicaRepository repository;

    public ServiceSolicitudes(IDAO<Hecho> dao) {
        this.repository = new DinamicaRepository(dao);
    }

    public List<SolicitudEliminacion> obtenerSolicitudes() throws IOException {
        return repository.obtenerSolicitudes();
    }

    public List<SolicitudEliminacion> agregarSolicitudes(SolicitudEliminacion solicitud) throws IOException {
        return repository.agregarSolicitudes(solicitud);
    }
}
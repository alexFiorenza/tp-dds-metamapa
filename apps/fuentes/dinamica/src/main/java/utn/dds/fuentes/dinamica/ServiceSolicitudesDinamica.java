package utn.dds.fuentes.dinamica;

import utn.dds.daos.IDAO;
import utn.dds.dominio.DetectorSpam;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;

import java.io.IOException;
import java.util.List;

public class ServiceSolicitudesDinamica {
    private final HechoRepository repository;
    private final SolicitudEliminacionRepositoryDinamica solicitudDinamica;
    private final DetectorSpam detectorSpam;

    public ServiceSolicitudesDinamica(IDAO<Hecho> dao, SolicitudEliminacionRepositoryDinamica solicitudDinamica, DetectorSpam detectorSpam) {
        this.repository = new HechoRepository(dao);
        this.solicitudDinamica = solicitudDinamica;
        this.detectorSpam = detectorSpam;
    }


    public List<SolicitudEliminacion> obtenerSolicitudes(String uuid) throws IOException {
        return solicitudDinamica.obtenerSolicitudes(uuid);
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
        return solicitudDinamica.agregarSolicitud(solicitud);
    }

    public void aceptarSolicitud(String uuid) throws IOException {

    }

    public void rechazarSolicitud(String uuid) throws IOException {

    }
}
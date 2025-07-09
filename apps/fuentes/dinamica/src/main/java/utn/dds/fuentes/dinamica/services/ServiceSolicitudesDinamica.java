package utn.dds.fuentes.dinamica.services;

import utn.dds.daos.IDAO;
import utn.dds.dominio.DetectorSpam;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.fuentes.dinamica.repositories.HechoRepository;
import utn.dds.fuentes.dinamica.repositories.SolicitudEliminacionRepositoryDinamica;

import java.io.IOException;
import java.util.List;

public class ServiceSolicitudesDinamica {
    private final HechoRepository repository;
    private final SolicitudEliminacionRepositoryDinamica solicitudDinamicaRepo;
    private final DetectorSpam detectorSpam;

    public ServiceSolicitudesDinamica(IDAO<HechoDTO> daoHecho, IDAO<SolicitudEliminacionDTO> daoSolicitud, SolicitudEliminacionRepositoryDinamica solicitudDinamicaRepo, DetectorSpam detectorSpam) {
        this.repository = new HechoRepository(daoHecho);
        this.solicitudDinamicaRepo = new SolicitudEliminacionRepositoryDinamica(daoSolicitud);
        this.detectorSpam = detectorSpam;
    }

    public List<SolicitudEliminacion> obtenerSolicitudes() throws IOException {
        return solicitudDinamicaRepo.obtenerSolicitudes();
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
        return solicitudDinamicaRepo.agregarSolicitud(solicitud);
    }

    public void aceptarSolicitud(String uuid) throws IOException {
        // Tenemos que usar el respository de Solicitudes y el de Hecho
        SolicitudEliminacion solicitud = solicitudDinamicaRepo.obtenerSolicitud(uuid);
        Hecho hecho = repository.buscarHecho(solicitud.getHecho());
        hecho.ocultar();
    }

    /// //////////////////////////////////////////////////////////////////////////////////
    ///  Creo que en repository no hay que modificar las solicitudes,  solo buscarlas  ///
    /// //////////////////////////////////////////////////////////////////////////////////

    public void rechazarSolicitud(String uuid) throws IOException {
        // Aca no se hace nada creo
        //preguntar.........
    }
}
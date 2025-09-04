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

    public ServiceSolicitudesDinamica(IDAO<HechoDTO> daoHecho, IDAO<SolicitudEliminacion> daoSolicitud, DetectorSpam detectorSpam) {
        this.repository = new HechoRepository(daoHecho);
        this.solicitudDinamicaRepo = new SolicitudEliminacionRepositoryDinamica(daoSolicitud);
        this.detectorSpam = detectorSpam;
    }

    public List<SolicitudEliminacion> obtenerSolicitudes() throws IOException {
        return this.solicitudDinamicaRepo.obtenerSolicitudes();
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
        return solicitudDinamicaRepo.agregarSolicitud(solicitud);
    }

    public void aceptarSolicitud(String uuid) throws IOException {
        solicitudDinamicaRepo.procesarSolicitud(uuid);
        SolicitudEliminacion solicitud = solicitudDinamicaRepo.obtenerSolicitud(uuid);

        // Buscamos el hecho
        Hecho hecho = repository.buscarHecho(solicitud.getHecho());

        // Ocultamos el hecho
        repository.cambiarEstado(hecho);
    }

    public void rechazarSolicitud(String uuid) throws IOException {
        solicitudDinamicaRepo.procesarSolicitud(uuid);
        // Aca no se hace nada creo
        //preguntar.........
    }
}
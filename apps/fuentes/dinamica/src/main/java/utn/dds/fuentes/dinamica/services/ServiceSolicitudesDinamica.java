package utn.dds.fuentes.dinamica.services;

import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.daos.IDAO;
import utn.dds.dominio.DetectorSpam;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.fuentes.dinamica.controllers.ControllerHechoDinamica;
import utn.dds.fuentes.dinamica.repositories.HechoRepository;
import utn.dds.fuentes.dinamica.repositories.SolicitudEliminacionRepositoryDinamica;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ServiceSolicitudesDinamica {
    private final HechoRepository repository;
    private final SolicitudEliminacionRepositoryDinamica solicitudDinamicaRepo;
    private static final Logger loggerServiceSolicitudes = LoggerFactory.getLogger(ServiceSolicitudesDinamica.class);
    // private final DetectorSpam detectorSpam;

    public ServiceSolicitudesDinamica(String daoType, Map<String, Object> daoConfig) {
        this.repository = new HechoRepository(daoType, daoConfig);
        this.solicitudDinamicaRepo = new SolicitudEliminacionRepositoryDinamica(daoType, daoConfig);
        //this.detectorSpam = detectorSpam;
    }

    public List<SolicitudEliminacion> obtenerSolicitudes() throws IOException {
        return this.solicitudDinamicaRepo.obtenerSolicitudes();
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
        return this.solicitudDinamicaRepo.agregarSolicitud(solicitud);
    }


    public void aceptarSolicitud(String uuid) throws IOException {
        this.solicitudDinamicaRepo.procesarSolicitud(uuid);
        SolicitudEliminacion solicitud = this.solicitudDinamicaRepo.obtenerSolicitud(uuid);

        // Buscamos el hecho
        Hecho hecho = this.repository.buscarHecho(solicitud.getHecho());

        // Ocultamos el hecho
        this.repository.cambiarEstado(hecho);
    }

    public RespuestaPaginadaDTO<SolicitudEliminacion> obtenerSolicitudesPaginadas(int pagina, int tamanioPagina) throws IOException {
        // Validaciones
        if (pagina < 0) {
            pagina = 0;
        }
        if (tamanioPagina <= 0) {
            tamanioPagina = 10; // Tamaño por defecto
        }
        if (tamanioPagina > 100) {
            tamanioPagina = 100; // Máximo 100 elementos por página
        }

        // Obtener todos los hechos
        List<SolicitudEliminacion> todasLasSolicitudes = solicitudDinamicaRepo.obtenerSolicitudes();
        long totalElementos = todasLasSolicitudes.size();

        // Calcular índices para la paginación
        int indiceInicio = pagina * tamanioPagina;
        int indiceFin = Math.min(indiceInicio + tamanioPagina, (int) totalElementos);

        // Obtener datos de la página actual
        List<SolicitudEliminacion> datosPagina;
        if (indiceInicio >= totalElementos) {
            datosPagina = new ArrayList<>();
        } else {
            datosPagina = todasLasSolicitudes.subList(indiceInicio, indiceFin);
        }

        return new RespuestaPaginadaDTO<>(datosPagina, pagina, tamanioPagina, totalElementos);
    }

    public void rechazarSolicitud(String uuid) throws IOException {
        this.solicitudDinamicaRepo.procesarSolicitud(uuid);
        // Aca no se hace nada creo
        //preguntar.........
    }


}
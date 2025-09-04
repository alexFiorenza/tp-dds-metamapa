package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.fuentes.dinamica.Main;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import utn.dds.fuentes.dinamica.services.ServiceSolicitudesDinamica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerSolicitudesDinamica {
    private final ServiceSolicitudesDinamica solicitudesService;
    private static final Logger loggerSolicitudes = LoggerFactory.getLogger(ControllerSolicitudesDinamica.class);

    public ControllerSolicitudesDinamica(String daoType, Map<String, Object> daoConfig){
        this.solicitudesService = new ServiceSolicitudesDinamica(daoType, daoConfig);
    }

    public void obtenerSolicitudesDeEliminacion(Context ctx){
        try {
            loggerSolicitudes.info("-----------Obteniendo Solicitudes de Eliminación-----------");
            List<SolicitudEliminacion> solicitudesEliminacion = this.solicitudesService.obtenerSolicitudes();

            loggerSolicitudes.info("Tamaño solicitud eliminacion es: " + solicitudesEliminacion.size());

            List<SolicitudEliminacionDTO> solicitudesDTO = solicitudesEliminacion.stream()
                    .map(SolicitudEliminacionDTO::fromSolicitudEliminacion)
                    .collect(Collectors.toList());
            ctx.json(solicitudesDTO);
            loggerSolicitudes.info("Se obtuvieron exitosamente las solicitudes de eliminación");
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener solicitudes de eliminacion: " + e.getMessage());
        }
    }

    public void agregarSolicitudDeEliminacionDeHecho(Context ctx) {
        try {
            loggerSolicitudes.info("-----------Agregando Solicitud de Eliminación-----------");
            SolicitudEliminacionDTO solicitudDTO = ctx.bodyAsClass(SolicitudEliminacionDTO.class);
            SolicitudEliminacion solicitud = solicitudDTO.toSolicitudEliminacion();
            this.solicitudesService.agregarSolicitud(solicitud);
            ctx.json(solicitudDTO);
            loggerSolicitudes.info("Se agrego exitosamente la solicitud de eliminación");
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar solicitud de eliminacion: " + e.getMessage());
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    ////// esta parte seria manejada por un administrador, asi que no se si va en controller  ///////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    public void aceptarSolicitud(String uuid, Context ctx){
        try {
            this.solicitudesService.aceptarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al aceptar solicitud: " + e.getMessage());
        }
    }

    public void rechazarSolicitud(String uuid, Context ctx){
        try {
            this.solicitudesService.rechazarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al rechazar solicitud: " + e.getMessage());
        }
    }
}


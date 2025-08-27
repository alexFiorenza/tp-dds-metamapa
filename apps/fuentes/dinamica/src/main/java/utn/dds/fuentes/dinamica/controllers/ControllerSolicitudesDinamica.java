package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.fuentes.dinamica.services.ServiceSolicitudesDinamica;

import java.util.List;
import java.util.stream.Collectors;

public class ControllerSolicitudesDinamica {
    private final ServiceSolicitudesDinamica solicitudesService;

    public ControllerSolicitudesDinamica(ServiceSolicitudesDinamica solicitudesService){this.solicitudesService = solicitudesService;}

    public void obtenerSolicitudesDeEliminacion(Context ctx){
        try {
            List<SolicitudEliminacion> solicitudesEliminacion = solicitudesService.obtenerSolicitudes();
            List<SolicitudEliminacionDTO> solicitudesDTO = solicitudesEliminacion.stream()
                    .map(SolicitudEliminacionDTO::fromSolicitudEliminacion)
                    .collect(Collectors.toList());
            ctx.json(solicitudesDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener solicitudes de eliminacion: " + e.getMessage());
        }
    }

    public void agregarSolicitudDeEliminacionDeHecho(Context ctx) {
        try {
            SolicitudEliminacionDTO solicitudDTO = ctx.bodyAsClass(SolicitudEliminacionDTO.class);
            SolicitudEliminacion solicitud = solicitudDTO.toSolicitudEliminacion();
            solicitudesService.agregarSolicitud(solicitud);
            ctx.json(solicitudDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar solicitud de eliminacion: " + e.getMessage());
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    ////// esta parte seria manejada por un administrador, asi que no se si va en controller  ///////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    public void aceptarSolicitud(String uuid, Context ctx){
        try {
            solicitudesService.aceptarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al aceptar solicitud: " + e.getMessage());
        }
    }

    public void rechazarSolicitud(String uuid, Context ctx){
        try {
            solicitudesService.rechazarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al rechazar solicitud: " + e.getMessage());
        }
    }
}


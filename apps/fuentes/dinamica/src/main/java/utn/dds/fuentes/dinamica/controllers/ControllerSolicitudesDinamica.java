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

    public void agregarSolicitudDeEliminacionDeHecho(Context ctx, SolicitudEliminacion solicitud) {
        try {
            SolicitudEliminacionDTO solicitudDTO = SolicitudEliminacionDTO.fromSolicitudEliminacion(solicitud);
            ctx.json(solicitudDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar solicitud de eliminacion: " + e.getMessage());
        }
    }

    public void aceptarSolicitud(String uuid, Context ctx){
        try {
            ctx.json(solicitud);
        } catch (Exception e) {
            ctx.status(500).result("Error al aceptar solicitud: " + e.getMessage());
        }

    }

    public void rechazarSolicitud(String uuid, Context ctx){
        try {
            ctx.json(solicitud); // Esto hay que verlo, porque no se si se le enviaria un JSON de la solicitud
        } catch (Exception e) {
            ctx.status(500).result("Error al rechazar solicitud: " + e.getMessage());
        }
    }
}


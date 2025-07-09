package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.fuentes.dinamica.services.ServiceSolicitudesDinamica;

public class ControllerSolicitudesDinamica {
    private final ServiceSolicitudesDinamica solicitudesService;

    public ControllerSolicitudesDinamica(ServiceSolicitudesDinamica solicitudesService){this.solicitudesService = solicitudesService;}

    // Esta no se si iria
    public void obtenerSolicitudesDeEliminacion(Context ctx){}

    public void agregarSolicitudDeEliminacionDeHecho(Context ctx, SolicitudEliminacion solicitud) {
        try {
            ctx.json(solicitud); // Esto hay que verlo, porque no se si se le enviaria un JSON de la solicitud
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar solicitud de eliminacion: " + e.getMessage());
        }
    }
}

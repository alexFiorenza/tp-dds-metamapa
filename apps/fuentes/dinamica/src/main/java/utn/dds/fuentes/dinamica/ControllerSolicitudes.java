package utn.dds.fuentes.dinamica;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.dinamica.ServiceFuenteDinamica;

import java.util.List;
import java.util.stream.Collectors;
import io.javalin.http.Context;
import utn.dds.dominio.SolicitudEliminacion;

public class ControllerSolicitudes {
    private final ServiceSolicitudes solicitudesService;

    public ControllerSolicitudes(ServiceSolicitudes solicitudesService){this.solicitudesService = solicitudesService;}

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

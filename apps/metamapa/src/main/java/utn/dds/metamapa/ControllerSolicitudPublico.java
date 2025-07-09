package utn.dds.metamapa;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import utn.dds.dominio.SolicitudEliminacion;


public class ControllerSolicitudPublico {
    private final ServiceSolicitudEliminacionMetamapa serviceSolicitudEliminacion;
    public  ControllerSolicitudPublico(){
        this.serviceSolicitudEliminacion = new ServiceSolicitudEliminacionMetamapa();
    }
    public void agregarSolicitud(@NotNull Context ctx) {
        SolicitudEliminacion solicitud = ctx.bodyAsClass(SolicitudEliminacion.class);
        serviceSolicitudEliminacion.procesarSolicitud(solicitud);
        ctx.status(201).json(solicitud);
    }
}

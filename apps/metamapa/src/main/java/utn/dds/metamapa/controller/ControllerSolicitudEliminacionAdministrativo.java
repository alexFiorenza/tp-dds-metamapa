package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import utn.dds.metamapa.service.ServiceSolicitudEliminacion;
import utn.dds.dto.SolicitudEliminacionDTO;

import java.util.Map;

public class ControllerSolicitudEliminacionAdministrativo {
    private final ServiceSolicitudEliminacion serviceSolicitudEliminacion;

    public ControllerSolicitudEliminacionAdministrativo(String daoType, Map<String, Object> daoConfig) {
        this.serviceSolicitudEliminacion = new ServiceSolicitudEliminacion(daoType, daoConfig);
    }

    public void aceptarSolicitud(Context ctx) {
        try {
            String uuid = ctx.pathParam("uuid");
            this.serviceSolicitudEliminacion.aceptar(uuid);
            ctx.status(200).result("Solicitud aceptada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al aceptar solicitud: " + e.getMessage());
        }
    }

    public void rechazarSolicitud(Context ctx) {
        try {
            String uuid = ctx.pathParam("uuid");
            this.serviceSolicitudEliminacion.rechazar(uuid);
            ctx.status(200).result("Solicitud rechazada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al rechazar solicitud: " + e.getMessage());
        }
    }
}
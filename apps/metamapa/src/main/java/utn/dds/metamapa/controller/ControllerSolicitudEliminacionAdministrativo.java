package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceSolicitudEliminacion;
import utn.dds.dto.SolicitudEliminacionDTO;

import java.util.Map;

public class ControllerSolicitudEliminacionAdministrativo {
    private final ServiceSolicitudEliminacion serviceSolicitudEliminacion;

    public ControllerSolicitudEliminacionAdministrativo(String daoType, Map<String, Object> daoConfig) {
        this.serviceSolicitudEliminacion = new ServiceSolicitudEliminacion(daoType, daoConfig);
    }

    @OpenApi(
        summary = "Aceptar solicitud de eliminación",
        operationId = "aceptarSolicitud",
        path = "/administrador/solicitud/{uuid}/aceptar",
        methods = HttpMethod.PUT,
        tags = {"Administrador - Solicitudes"},
        pathParams = @OpenApiParam(name = "uuid", description = "UUID de la solicitud"),
        responses = {
            @OpenApiResponse(status = "200", description = "Solicitud aceptada exitosamente"),
            @OpenApiResponse(status = "400", description = "Error al aceptar solicitud")
        }
    )
    public void aceptarSolicitud(Context ctx) {
        try {
            String uuid = ctx.pathParam("uuid");
            this.serviceSolicitudEliminacion.aceptar(uuid);
            ctx.status(200).result("Solicitud aceptada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al aceptar solicitud: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Rechazar solicitud de eliminación",
        operationId = "rechazarSolicitud",
        path = "/administrador/solicitud/{uuid}/rechazar",
        methods = HttpMethod.PUT,
        tags = {"Administrador - Solicitudes"},
        pathParams = @OpenApiParam(name = "uuid", description = "UUID de la solicitud"),
        responses = {
            @OpenApiResponse(status = "200", description = "Solicitud rechazada exitosamente"),
            @OpenApiResponse(status = "400", description = "Error al rechazar solicitud")
        }
    )
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
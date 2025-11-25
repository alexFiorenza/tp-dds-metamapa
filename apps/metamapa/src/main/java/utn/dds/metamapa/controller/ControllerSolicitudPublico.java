package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceSolicitudEliminacion;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoSolicitud;
import utn.dds.dominio.DetectorSpam;
import utn.dds.dominio.AlgoritmoTFIDF;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.dto.SolicitudEliminacionRequestDTO;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public class ControllerSolicitudPublico {
    private final ServiceSolicitudEliminacion serviceSolicitudEliminacion;
    private final DetectorSpam detectorSpam;

    public ControllerSolicitudPublico(String daoType, Map<String, Object> daoConfig) {
        this.serviceSolicitudEliminacion = new ServiceSolicitudEliminacion(daoType, daoConfig);
        this.detectorSpam = new AlgoritmoTFIDF();
    }

    @OpenApi(
        summary = "Crear solicitud de eliminación de un hecho",
        operationId = "crearSolicitudEliminacion",
        path = "/api/solicitudes",
        methods = HttpMethod.POST,
        tags = {"API Pública - Solicitudes"},
        requestBody = @OpenApiRequestBody(
            description = "Datos de la solicitud de eliminación",
            content = @OpenApiContent(from = SolicitudEliminacionRequestDTO.class)
        ),
        responses = {
            @OpenApiResponse(status = "201", description = "Solicitud creada exitosamente", content = @OpenApiContent(from = SolicitudEliminacionDTO.class)),
            @OpenApiResponse(status = "400", description = "Error al crear solicitud")
        }
    )
    public void crearSolicitudEliminacion(Context ctx) {
        try {
            SolicitudEliminacionRequestDTO request = ctx.bodyAsClass(SolicitudEliminacionRequestDTO.class);

            // Validar datos requeridos
            if (request.getTexto() == null || request.getTexto().isEmpty()) {
                ctx.status(400).result("El texto de la solicitud es requerido");
                return;
            }

            if (request.getHecho() == null || request.getHecho().isEmpty()) {
                ctx.status(400).result("El UUID del hecho es requerido");
                return;
            }

            // Detectar spam
            if (detectorSpam.esSpam(request.getTexto())) {
                ctx.status(400).result("La solicitud fue detectada como spam y fue rechazada automáticamente");
                return;
            }

            // Crear la solicitud
            String uuidSolicitud = UUID.randomUUID().toString();
            SolicitudEliminacion solicitud = new SolicitudEliminacion(
                request.getTexto(),
                request.getHecho(),
                LocalDateTime.now(),
                EstadoSolicitud.ACTIVO,
                uuidSolicitud
            );

            this.serviceSolicitudEliminacion.crearSolicitud(solicitud);

            // Devolver la solicitud creada
            SolicitudEliminacionDTO solicitudDTO = SolicitudEliminacionDTO.fromSolicitudEliminacion(solicitud);
            ctx.status(201).json(solicitudDTO);

        } catch (Exception e) {
            ctx.status(400).result("Error al crear solicitud: " + e.getMessage());
        }
    }
}
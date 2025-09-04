package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
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

    @OpenApi(
        summary = "Obtener solicitudes de eliminación",
        operationId = "obtenerSolicitudesEliminacion",
        path = "/solicitudes",
        methods = HttpMethod.GET,
        tags = {"Solicitudes de Eliminación"},
        description = "Obtiene solicitudes de eliminación con paginación. Soporta paginación con valores por defecto.",
        queryParams = {
            @OpenApiParam(
                name = "pagina",
                description = "Número de página (empezando desde 0). Por defecto 0",
                required = false,
                type = Integer.class
            ),
            @OpenApiParam(
                name = "tamanio",
                description = "Tamaño de página (máximo 100, por defecto 10)",
                required = false,
                type = Integer.class
            )
        },
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Solicitudes obtenidas exitosamente con paginación",
                content = {@OpenApiContent(from = RespuestaPaginadaDTO.class)}
            ),
            @OpenApiResponse(status = "400", description = "Error en parámetros de paginación"),
            @OpenApiResponse(status = "500", description = "Error al procesar la solicitud")
        }
    )
    public void obtenerSolicitudesDeEliminacion(Context ctx){
        try {
            loggerSolicitudes.info("-----------Obteniendo Solicitudes de Eliminación-----------");
            String paginaParam = ctx.queryParam("pagina");
            String tamanioParam = ctx.queryParam("tamanio");

            // Siempre usar paginación, con valores por defecto si no se especifican
            int pagina = paginaParam != null ? Integer.parseInt(paginaParam) : 0;
            int tamanio = tamanioParam != null ? Integer.parseInt(tamanioParam) : 10;

            RespuestaPaginadaDTO<SolicitudEliminacion> respuestaPaginada = solicitudesService.obtenerSolicitudesPaginadas(pagina, tamanio);

            // Convertir las Solicitudes a DTO
            List<SolicitudEliminacionDTO> SolicitudesDTO = respuestaPaginada.getDatos().stream()
                    .map(SolicitudEliminacionDTO::fromSolicitudEliminacion)
                    .collect(Collectors.toList());


            // Crear respuesta paginada con DTOs
            RespuestaPaginadaDTO<SolicitudEliminacionDTO> respuestaDTOPaginada = new RespuestaPaginadaDTO<>(
                    SolicitudesDTO,
                    respuestaPaginada.getPagina(),
                    respuestaPaginada.getTamanioPagina(),
                    respuestaPaginada.getTotalElementos()
            );

            ctx.json(respuestaDTOPaginada);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Error en parámetros de paginación: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Agregar nueva solicitud de eliminación",
        operationId = "agregarSolicitudEliminacion",
        path = "/solicitudes",
        methods = HttpMethod.POST,
        tags = {"Solicitudes de Eliminación"},
        description = "Crea una nueva solicitud de eliminación de hecho",
        requestBody = @OpenApiRequestBody(
            description = "Datos de la solicitud de eliminación",
            content = {@OpenApiContent(from = SolicitudEliminacionDTO.class)}
        ),
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Solicitud creada exitosamente",
                content = {@OpenApiContent(from = SolicitudEliminacionDTO.class)}
            ),
            @OpenApiResponse(status = "400", description = "Datos de la solicitud inválidos"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
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

    @OpenApi(
        summary = "Aceptar solicitud de eliminación",
        operationId = "aceptarSolicitudEliminacion",
        path = "/solicitudes/{uuid}/aceptar",
        methods = HttpMethod.PUT,
        tags = {"Administración"},
        description = "Acepta una solicitud de eliminación específica (requiere permisos de administrador)",
        pathParams = {
            @OpenApiParam(
                name = "uuid",
                description = "UUID de la solicitud de eliminación",
                required = true,
                type = String.class
            )
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Solicitud aceptada exitosamente"),
            @OpenApiResponse(status = "404", description = "Solicitud no encontrada"),
            @OpenApiResponse(status = "500", description = "Error al procesar la solicitud")
        }
    )
    public void aceptarSolicitud(String uuid, Context ctx){
        try {
            this.solicitudesService.aceptarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al aceptar solicitud: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Rechazar solicitud de eliminación",
        operationId = "rechazarSolicitudEliminacion",
        path = "/solicitudes/{uuid}/rechazar",
        methods = HttpMethod.PUT,
        tags = {"Administración"},
        description = "Rechaza una solicitud de eliminación específica (requiere permisos de administrador)",
        pathParams = {
            @OpenApiParam(
                name = "uuid",
                description = "UUID de la solicitud de eliminación",
                required = true,
                type = String.class
            )
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Solicitud rechazada exitosamente"),
            @OpenApiResponse(status = "404", description = "Solicitud no encontrada"),
            @OpenApiResponse(status = "500", description = "Error al procesar la solicitud")
        }
    )
    public void rechazarSolicitud(String uuid, Context ctx){
        try {
            this.solicitudesService.rechazarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al rechazar solicitud: " + e.getMessage());
        }
    }

}


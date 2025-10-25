package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.persistencia.FuentesRepository;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.UUID;

public class ControllerFuentesAdministrativo {
    private static final Logger logger = LoggerFactory.getLogger(ControllerFuentesAdministrativo.class);
    private final FuentesRepository fuentesRepository;

    public ControllerFuentesAdministrativo(Map<String, Object> daoConfig) {
        this.fuentesRepository = new FuentesRepository(daoConfig);
    }

    @OpenApi(
        summary = "Obtener todas las fuentes registradas (paginado)",
        operationId = "obtenerFuentes",
        path = "/administrador/fuentes",
        methods = HttpMethod.GET,
        tags = {"Administrador - Fuentes"},
        security = {@OpenApiSecurity(name = "BearerAuth")},
        queryParams = {
            @OpenApiParam(name = "page", description = "Número de página (default: 0)"),
            @OpenApiParam(name = "size", description = "Tamaño de página (default: 10, max: 100)")
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "Lista paginada de fuentes registradas",
                content = @OpenApiContent(from = RespuestaPaginadaDTO.class)
            ),
            @OpenApiResponse(status = "401", description = "No autenticado"),
            @OpenApiResponse(status = "403", description = "No tiene permisos de administrador"),
            @OpenApiResponse(status = "500", description = "Error al obtener fuentes")
        }
    )
    public void obtenerFuentes(Context ctx) {
        try {
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);

            // Validar tamaño de página
            if (size > 100) {
                size = 100;
            }

            RespuestaPaginadaDTO<FuenteDTO> respuesta = fuentesRepository.findPaginado(page, size);
            ctx.status(200).json(respuesta);
        } catch (Exception e) {
            logger.error("Error al obtener fuentes", e);
            ctx.status(500).result("Error al obtener fuentes: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Obtener fuente por UUID",
        operationId = "obtenerFuentePorUuid",
        path = "/administrador/fuentes/{uuid}",
        methods = HttpMethod.GET,
        tags = {"Administrador - Fuentes"},
        security = {@OpenApiSecurity(name = "BearerAuth")},
        pathParams = @OpenApiParam(name = "uuid", description = "UUID de la fuente", required = true),
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "Fuente encontrada",
                content = @OpenApiContent(from = FuenteDTO.class)
            ),
            @OpenApiResponse(status = "400", description = "UUID inválido"),
            @OpenApiResponse(status = "401", description = "No autenticado"),
            @OpenApiResponse(status = "403", description = "No tiene permisos de administrador"),
            @OpenApiResponse(status = "404", description = "Fuente no encontrada"),
            @OpenApiResponse(status = "500", description = "Error al obtener fuente")
        }
    )
    public void obtenerFuentePorUuid(Context ctx) {
        try {
            String uuidStr = ctx.pathParam("uuid");
            UUID uuid;

            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                logger.error("UUID inválido: {}", uuidStr);
                ctx.status(400).result("UUID inválido: " + uuidStr);
                return;
            }

            FuenteDTO fuente = fuentesRepository.findByUuid(uuid);

            if (fuente != null) {
                ctx.status(200).json(fuente);
            } else {
                ctx.status(404).result("Fuente no encontrada");
            }
        } catch (Exception e) {
            logger.error("Error al obtener fuente por UUID", e);
            ctx.status(500).result("Error al obtener fuente: " + e.getMessage());
        }
    }
}

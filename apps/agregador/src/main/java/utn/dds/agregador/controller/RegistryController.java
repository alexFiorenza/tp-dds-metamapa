package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.agregador.service.ServiceRegistry;
import utn.dds.dto.FuenteDTO;
import java.util.UUID;
import io.javalin.openapi.*;
import java.util.List;

public class RegistryController {
    
    private ServiceRegistry serviceAgregador;
    
    public RegistryController(ServiceRegistry serviceAgregador) {
        this.serviceAgregador = serviceAgregador;
    }
    
    @OpenApi(
        summary = "Registrar nueva fuente de datos",
        operationId = "registrarFuente",
        path = "/fuentes",
        methods = HttpMethod.POST,
        tags = {"Service Registry"},
        description = "Registra una nueva fuente. El UUID se genera automáticamente, no debe enviarse en el request.",
        requestBody = @OpenApiRequestBody(
            description = "Datos de la fuente a registrar (host y params). El UUID se genera automáticamente.",
            content = {@OpenApiContent(from = FuenteDTO.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", description = "Fuente registrada exitosamente"),
            @OpenApiResponse(status = "400", description = "Datos de fuente inválidos o host duplicado"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void registrar(Context ctx) {
        try {
            FuenteDTO fuente = ctx.bodyAsClass(FuenteDTO.class);
            serviceAgregador.registrar(fuente);
            ctx.status(201).result("Fuente registrada exitosamente");
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace(); // Log the actual error
            ctx.status(500).result("Error interno del servidor: " + e.getMessage());
        }
    }
    
    @OpenApi(
        summary = "Obtener todas las fuentes registradas",
        operationId = "obtenerFuentes",
        path = "/fuentes",
        methods = HttpMethod.GET,
        tags = {"Service Registry"},
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Lista de fuentes registradas",
                content = {@OpenApiContent(from = FuenteDTO[].class)}
            ),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void obtenerFuentes(Context ctx) {
        try {
            ctx.json(serviceAgregador.obtenerTodasLasFuentes());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener las fuentes");
        }
    }
    
    @OpenApi(
        summary = "Obtener fuente específica por host",
        operationId = "obtenerFuentePorHost",
        path = "/fuentes/{host}",
        methods = HttpMethod.GET,
        tags = {"Service Registry"},
        pathParams = {
            @OpenApiParam(name = "host", description = "Host de la fuente", required = true)
        },
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Fuente encontrada",
                content = {@OpenApiContent(from = FuenteDTO.class)}
            ),
            @OpenApiResponse(status = "404", description = "Fuente no encontrada"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void obtenerFuentePorHost(Context ctx) {
        try {
            String host = ctx.pathParam("host");
            FuenteDTO fuente = serviceAgregador.obtenerFuentePorHost(host);
            if (fuente != null) {
                ctx.json(fuente);
            } else {
                ctx.status(404).result("Fuente no encontrada");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener la fuente");
        }
    }
    
    @OpenApi(
        summary = "Eliminar fuente de datos",
        operationId = "eliminarFuente",
        path = "/fuentes/{uuid}",
        methods = HttpMethod.DELETE,
        tags = {"Service Registry"},
        pathParams = {
            @OpenApiParam(name = "uuid", description = "UUID único de la fuente a eliminar", required = true)
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Fuente eliminada exitosamente"),
            @OpenApiResponse(status = "400", description = "UUID inválido"),
            @OpenApiResponse(status = "404", description = "Fuente no encontrada"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void eliminarFuente(Context ctx) {
        try {
            String uuidParam = ctx.pathParam("uuid");
            UUID uuid = UUID.fromString(uuidParam);
            boolean eliminada = serviceAgregador.eliminarFuentePorUuid(uuid);
            if (eliminada) {
                ctx.status(200).result("Fuente eliminada exitosamente");
            } else {
                ctx.status(404).result("Fuente no encontrada");
            }
        } catch (IllegalArgumentException e) {
            ctx.status(400).result("UUID inválido: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error al eliminar la fuente");
        }
    }
}

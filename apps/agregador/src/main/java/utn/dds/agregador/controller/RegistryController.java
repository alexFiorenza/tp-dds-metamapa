package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.agregador.service.ServiceRegistry;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.FuenteRequestDTO;
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
        description = "Registra una nueva fuente con esquema flexible de URLs. El UUID se genera automáticamente.\n\n" +
                    "**Campos principales:**\n" +
                    "- `host`: URL base de la fuente (requerido)\n" +
                    "- `tipo`: Tipo de fuente - \"estatica\", \"proxy\" o \"dinamica\" (opcional)\n" +
                    "- `params`: Configuración de URL (opcional)\n" +
                    "- `metadata`: Metadatos específicos del tipo (opcional)\n\n" +
                    "**Esquema de params:**\n" +
                    "- `endpoint`: Ruta del API (ej: '/hechos', '/api/data')\n" +
                    "- `pathParams`: Array de parámetros para la ruta\n" +
                    "- `queryParams`: Objeto con parámetros query string\n" +
                    "- `headers`: Headers HTTP adicionales\n" +
                    "- `body`: Body para requests POST\n\n" +
                    "**Fuentes estáticas:** Si tipo=\"estatica\" y no se proporciona metadata, se inicializa con {\"procesado\": false}\n\n" +
                    "**Ejemplos:**\n" +
                    "1. Fuente estática: `{\"host\":\"http://localhost:8080\", \"tipo\":\"estatica\", \"params\":{\"endpoint\":\"/hechos\", \"queryParams\":{\"path\":\"archivo.csv\"}}}`\n" +
                    "2. Fuente dinámica: `{\"host\":\"http://api.example.com\", \"tipo\":\"dinamica\", \"params\":{\"endpoint\":\"/api/data\"}}`",
        requestBody = @OpenApiRequestBody(
            description = "Datos de la fuente a registrar con esquema flexible de URL construction.",
            content = {@OpenApiContent(from = FuenteRequestDTO.class)}
        ),
        responses = {
            @OpenApiResponse(status = "201", description = "Fuente registrada exitosamente"),
            @OpenApiResponse(status = "400", description = "Datos de fuente inválidos o host duplicado"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void registrar(Context ctx) {
        try {
            FuenteRequestDTO request = ctx.bodyAsClass(FuenteRequestDTO.class);

            // Convertir FuenteRequestDTO a FuenteDTO (el UUID se generará automáticamente)
            FuenteDTO fuente = new FuenteDTO(
                request.getHost(),
                request.getParams(),
                null,
                request.getTipo(),
                request.getMetadata()
            );

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
        summary = "Obtener fuentes por host",
        operationId = "obtenerFuentesPorHost",
        path = "/fuentes/{host}",
        methods = HttpMethod.GET,
        tags = {"Service Registry"},
        pathParams = {
            @OpenApiParam(name = "host", description = "Host de las fuentes", required = true)
        },
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Fuentes encontradas (puede ser lista vacía si no hay coincidencias)",
                content = {@OpenApiContent(from = FuenteDTO[].class)}
            ),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void obtenerFuentePorHost(Context ctx) {
        try {
            String host = ctx.pathParam("host");
            List<FuenteDTO> fuentes = serviceAgregador.obtenerFuentesPorHost(host);
            ctx.json(fuentes); // Siempre devuelve un array, aunque esté vacío
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener las fuentes");
        }
    }
    
    @OpenApi(
        summary = "Actualizar fuente de datos",
        operationId = "actualizarFuente",
        path = "/fuentes/{uuid}",
        methods = HttpMethod.PUT,
        tags = {"Service Registry"},
        description = "Actualiza una fuente existente. Permite modificar host, params, tipo y metadata (incluyendo el flag 'procesado' para fuentes estáticas).\n\n" +
                    "**Ejemplo para resetear fuente estática:**\n" +
                    "```json\n" +
                    "{\n" +
                    "  \"host\": \"http://localhost:8080\",\n" +
                    "  \"tipo\": \"estatica\",\n" +
                    "  \"metadata\": {\"procesado\": false}\n" +
                    "}\n" +
                    "```",
        pathParams = {
            @OpenApiParam(name = "uuid", description = "UUID único de la fuente a actualizar", required = true)
        },
        requestBody = @OpenApiRequestBody(
            description = "Datos actualizados de la fuente. Solo se actualizarán los campos proporcionados.",
            content = {@OpenApiContent(from = FuenteDTO.class)}
        ),
        responses = {
            @OpenApiResponse(status = "200", description = "Fuente actualizada exitosamente"),
            @OpenApiResponse(status = "400", description = "UUID inválido o datos incorrectos"),
            @OpenApiResponse(status = "404", description = "Fuente no encontrada"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void actualizarFuente(Context ctx) {
        try {
            String uuidParam = ctx.pathParam("uuid");
            UUID uuid = UUID.fromString(uuidParam);

            FuenteDTO fuenteActualizada = ctx.bodyAsClass(FuenteDTO.class);
            fuenteActualizada.setUuid(uuid); // Asegurar que el UUID del path se use

            serviceAgregador.actualizar(fuenteActualizada);
            ctx.status(200).result("Fuente actualizada exitosamente");
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("UUID") || e.getMessage().contains("uuid")) {
                ctx.status(400).result("UUID inválido: " + e.getMessage());
            } else if (e.getMessage().contains("No existe")) {
                ctx.status(404).result(e.getMessage());
            } else {
                ctx.status(400).result(e.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.status(500).result("Error al actualizar la fuente: " + e.getMessage());
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

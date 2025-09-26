package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceColeccion;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.ColeccionCreateDTO;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerColeccionAdministrativo {
    private final ServiceColeccion serviceColeccion;

    public ControllerColeccionAdministrativo(String daoType, Map<String, Object> daoConfig) {
        this.serviceColeccion = new ServiceColeccion(daoType, daoConfig);
    }

    @OpenApi(
        summary = "Obtener todas las colecciones",
        operationId = "obtenerColecciones",
        path = "/administrador/coleccion",
        methods = HttpMethod.GET,
        tags = {"Administrador - Colecciones"},
        queryParams = {
            @OpenApiParam(name = "page", description = "Número de página (default: 0)"),
            @OpenApiParam(name = "size", description = "Tamaño de página (default: 10, max: 100)")
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Lista paginada de colecciones", content = @OpenApiContent(from = RespuestaPaginadaDTO.class))
        }
    )
    public void obtenerColecciones(Context ctx) {
        int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
        int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);
        RespuestaPaginadaDTO<ColeccionDTO> respuesta = this.serviceColeccion.obtenerColeccionesPaginado(page, size);
        ctx.json(respuesta);
    }

    @OpenApi(
        summary = "Obtener colección por ID",
        operationId = "obtenerColeccionPorId",
        path = "/administrador/coleccion/{id}",
        methods = HttpMethod.GET,
        tags = {"Administrador - Colecciones"},
        pathParams = @OpenApiParam(name = "id", description = "ID de la colección"),
        responses = {
            @OpenApiResponse(status = "200", description = "Colección encontrada", content = @OpenApiContent(from = ColeccionDTO.class)),
            @OpenApiResponse(status = "404", description = "Colección no encontrada")
        }
    )
    public void obtenerColeccionPorId(Context ctx) {
        String id = ctx.pathParam("id");
        ColeccionDTO coleccionDTO = this.serviceColeccion.obtenerColeccionDTOPorId(id);
        if (coleccionDTO != null) {
            ctx.json(coleccionDTO);
        } else {
            ctx.status(404).result("Colección no encontrada");
        }
    }

    @OpenApi(
        summary = "Crear nueva colección",
        operationId = "crearColeccion",
        path = "/administrador/coleccion",
        methods = HttpMethod.POST,
        tags = {"Administrador - Colecciones"},
        requestBody = @OpenApiRequestBody(
            content = @OpenApiContent(
                from = ColeccionCreateDTO.class,
                example = "{\n" +
                    "  \"titulo\": \"Eventos Deportivos Buenos Aires\",\n" +
                    "  \"descripcion\": \"Colección de eventos deportivos que ocurrieron en la ciudad de Buenos Aires durante 2024\",\n" +
                    "  \"fuentesIds\": [\"fuente-gov-ar-123\", \"fuente-deportes-456\", \"fuente-estadios-789\"],\n" +
                    "  \"criteriosDePertenencia\": [\n" +
                    "    {\n" +
                    "      \"tipo\": \"categoria\",\n" +
                    "      \"categoria\": \"DEPORTES\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"tipo\": \"etiquetas\",\n" +
                    "      \"etiquetas\": \"buenos aires,argentina,evento\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"tipo\": \"estado\",\n" +
                    "      \"estado\": \"CONFIRMADO\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
            )
        ),
        responses = {
            @OpenApiResponse(status = "201", description = "Colección creada exitosamente"),
            @OpenApiResponse(status = "400", description = "Error al crear colección")
        }
    )
    public void crearColeccion(Context ctx) {
        try {
            ColeccionCreateDTO coleccionCreateDTO = ctx.bodyAsClass(ColeccionCreateDTO.class);
            this.serviceColeccion.crearColeccion(coleccionCreateDTO);
            ctx.status(201).result("Colección creada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al crear colección: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Actualizar colección existente",
        operationId = "actualizarColeccion",
        path = "/administrador/coleccion/{id}",
        methods = HttpMethod.PUT,
        tags = {"Administrador - Colecciones"},
        pathParams = @OpenApiParam(name = "id", description = "ID de la colección"),
        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Coleccion.class)),
        responses = {
            @OpenApiResponse(status = "200", description = "Colección actualizada exitosamente"),
            @OpenApiResponse(status = "400", description = "Error al actualizar colección")
        }
    )
    public void actualizarColeccion(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            Coleccion coleccionActualizada = ctx.bodyAsClass(Coleccion.class);
            this.serviceColeccion.actualizarColeccion(id, coleccionActualizada);
            ctx.status(200).result("Colección actualizada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al actualizar colección: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Eliminar colección",
        operationId = "eliminarColeccion",
        path = "/administrador/coleccion/{id}",
        methods = HttpMethod.DELETE,
        tags = {"Administrador - Colecciones"},
        pathParams = @OpenApiParam(name = "id", description = "ID de la colección"),
        responses = {
            @OpenApiResponse(status = "200", description = "Colección eliminada exitosamente"),
            @OpenApiResponse(status = "400", description = "Error al eliminar colección")
        }
    )
    public void eliminarColeccion(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            this.serviceColeccion.eliminarColeccion(id);
            ctx.status(200).result("Colección eliminada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al eliminar colección: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Obtener hechos de una colección con filtros opcionales",
        operationId = "obtenerHechosDeColeccion",
        path = "/administrador/coleccion/{id}/hechos",
        methods = HttpMethod.GET,
        tags = {"Administrador - Colecciones"},
        pathParams = @OpenApiParam(name = "id", description = "Handle de la colección"),
        queryParams = {
            @OpenApiParam(name = "page", description = "Número de página (default: 0)"),
            @OpenApiParam(name = "size", description = "Tamaño de página (default: 10, max: 100)"),
            @OpenApiParam(name = "categoria", description = "Filtrar por categoría específica", required = false),
            @OpenApiParam(name = "estado", description = "Filtrar por estado (CONFIRMADO, RECHAZADO, etc.)", required = false),
            @OpenApiParam(name = "etiquetas", description = "Filtrar por etiquetas (separadas por comas)", required = false)
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Lista paginada de hechos de la colección", content = @OpenApiContent(from = RespuestaPaginadaDTO.class)),
            @OpenApiResponse(status = "404", description = "Colección no encontrada"),
            @OpenApiResponse(status = "400", description = "Error al obtener hechos")
        }
    )
    public void obtenerHechosDeColeccion(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);

            // Obtener filtros opcionales de query params
            String categoria = ctx.queryParam("categoria");
            String estado = ctx.queryParam("estado");
            String etiquetas = ctx.queryParam("etiquetas");

            RespuestaPaginadaDTO<HechoDTO> respuesta = this.serviceColeccion.obtenerHechosDeColeccionPaginado(
                id, page, size, categoria, estado, etiquetas);
            ctx.json(respuesta);
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Colección no encontrada")) {
                ctx.status(404).result(e.getMessage());
            } else {
                ctx.status(400).result("Error al obtener hechos: " + e.getMessage());
            }
        } catch (Exception e) {
            ctx.status(400).result("Error al obtener hechos: " + e.getMessage());
        }
    }

}
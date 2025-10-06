package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceColeccion;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dominio.criterios.HechoStrategy;

import java.util.Map;
import java.util.List;

public class ControllerColeccionPublica {
    private final ServiceColeccion serviceColeccion;

    public ControllerColeccionPublica(String daoType, Map<String, Object> daoConfig) {
        this.serviceColeccion = new ServiceColeccion(daoType, daoConfig);
    }

    @OpenApi(
        summary = "Obtener colección por identificador",
        operationId = "obtenerColeccionPublica",
        path = "/api/colecciones/{identificador}",
        methods = HttpMethod.GET,
        tags = {"API Pública - Colecciones"},
        pathParams = @OpenApiParam(name = "identificador", description = "Identificador de la colección"),
        responses = {
            @OpenApiResponse(status = "200", description = "Colección encontrada", content = @OpenApiContent(from = ColeccionDTO.class)),
            @OpenApiResponse(status = "404", description = "Colección no encontrada")
        }
    )
    public void obtenerColeccion(Context ctx) {
        try {
            String identificador = ctx.pathParam("identificador");
            ColeccionDTO coleccionDTO = this.serviceColeccion.obtenerColeccionPorId(identificador);

            if (coleccionDTO != null) {
                ctx.json(coleccionDTO);
            } else {
                ctx.status(404).result("Colección no encontrada");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener colección: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Obtener hechos de una colección específica",
        operationId = "obtenerHechosDeColeccion",
        path = "/api/colecciones/{identificador}/hechos",
        methods = HttpMethod.GET,
        tags = {"API Pública - Colecciones"},
        pathParams = @OpenApiParam(name = "identificador", description = "Identificador de la colección"),
        queryParams = {
            @OpenApiParam(name = "page", description = "Número de página (default: 0)"),
            @OpenApiParam(name = "size", description = "Tamaño de página (default: 10, max: 100)"),
            @OpenApiParam(name = "categoria", description = "Filtrar por categoría"),
            @OpenApiParam(name = "titulo", description = "Filtrar por título"),
            @OpenApiParam(name = "descripcion", description = "Filtrar por descripción"),
            @OpenApiParam(name = "origen", description = "Filtrar por origen"),
            @OpenApiParam(name = "fechaAcontecimiento", description = "Filtrar por fecha de acontecimiento (formato: YYYY-MM-DD)"),
            @OpenApiParam(name = "longitud", description = "Filtrar por longitud exacta", type = Double.class),
            @OpenApiParam(name = "latitud", description = "Filtrar por latitud exacta", type = Double.class),
            @OpenApiParam(name = "estado", description = "Filtrar por estado (ACTIVO, OCULTO)"),
            @OpenApiParam(name = "fechaCarga", description = "Filtrar por fecha de carga (formato: YYYY-MM-DDTHH:mm:ss)"),
            @OpenApiParam(name = "etiquetas", description = "Filtrar por etiquetas (separadas por comas)")
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Lista paginada de hechos de la colección", content = @OpenApiContent(from = RespuestaPaginadaDTO.class)),
            @OpenApiResponse(status = "404", description = "Colección no encontrada"),
            @OpenApiResponse(status = "400", description = "Error al buscar hechos")
        }
    )
    public void obtenerHechosDeColeccion(Context ctx) {
        try {
            String identificador = ctx.pathParam("identificador");
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);

            // Validar tamaño de página
            if (size > 100) {
                size = 100;
            }

            // Crear filtros usando el Factory
            List<HechoStrategy> filtros = FiltroFactory.crearFiltros(ctx);

            RespuestaPaginadaDTO<HechoDTO> respuesta = this.serviceColeccion.obtenerHechosDeColeccion(
                identificador, filtros, page, size);
            ctx.json(respuesta);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Error en parámetros: " + e.getMessage());
        } catch (RuntimeException e) {
            if (e.getMessage().equals("Colección no encontrada")) {
                ctx.status(404).result(e.getMessage());
            } else {
                ctx.status(400).result("Error al obtener hechos: " + e.getMessage());
            }
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }
}
package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceHechoMetamapa;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ControllerHechoPublico {
    private final ServiceHechoMetamapa serviceHecho;

    public ControllerHechoPublico(String daoType, Map<String, Object> daoConfig) {
        this.serviceHecho = new ServiceHechoMetamapa(daoType, daoConfig);
    }

    @OpenApi(
        summary = "Obtener hechos con filtros opcionales y paginación",
        operationId = "obtenerHechos",
        path = "/api/hechos",
        methods = HttpMethod.GET,
        tags = {"API Pública - Hechos"},
        queryParams = {
            @OpenApiParam(name = "categoria", description = "Filtrar por categoría"),
            @OpenApiParam(name = "titulo", description = "Filtrar por título"),
            @OpenApiParam(name = "descripcion", description = "Filtrar por descripción"),
            @OpenApiParam(name = "origen", description = "Filtrar por origen"),
            @OpenApiParam(name = "fechaAcontecimiento", description = "Filtrar por fecha de acontecimiento (formato: YYYY-MM-DD)"),
            @OpenApiParam(name = "longitud", description = "Filtrar por longitud exacta", type = Double.class),
            @OpenApiParam(name = "latitud", description = "Filtrar por latitud exacta", type = Double.class),
            @OpenApiParam(name = "estado", description = "Filtrar por estado (ACTIVO, OCULTO)"),
            @OpenApiParam(name = "fechaCarga", description = "Filtrar por fecha de carga (formato: YYYY-MM-DDTHH:mm:ss)"),
            @OpenApiParam(name = "etiquetas", description = "Filtrar por etiquetas (separadas por comas)"),
            @OpenApiParam(
                name = "pagina",
                description = "Número de página (comenzando desde 0). Por defecto: 0",
                required = false,
                type = Integer.class
            ),
            @OpenApiParam(
                name = "tamanioPagina",
                description = "Cantidad de elementos por página (min: 1, max: 100). Por defecto: 10",
                required = false,
                type = Integer.class
            )
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Hechos paginados obtenidos exitosamente", content = @OpenApiContent(from = RespuestaPaginadaDTO.class)),
            @OpenApiResponse(status = "400", description = "Error al buscar hechos")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            // Obtener parámetros de paginación
            int pagina = ctx.queryParamAsClass("pagina", Integer.class).getOrDefault(0);
            int tamanioPagina = ctx.queryParamAsClass("tamanioPagina", Integer.class).getOrDefault(10);

            // Validar tamaño de página
            if (tamanioPagina > 100) {
                tamanioPagina = 100;
            }

            // Crear filtros usando el Factory
            List<HechoStrategy> filtros = FiltroFactory.crearFiltros(ctx);

            RespuestaPaginadaDTO<HechoDTO> respuesta = this.serviceHecho.obtenerHechos(filtros, pagina, tamanioPagina);
            ctx.json(respuesta);

        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Error en parámetros: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Reportar un hecho como problemático",
        operationId = "reportarHecho",
        path = "/api/hechos/{uuid}/reportar",
        methods = HttpMethod.POST,
        tags = {"API Pública - Hechos"},
        pathParams = @OpenApiParam(name = "uuid", description = "UUID del hecho"),
        responses = {
            @OpenApiResponse(status = "200", description = "Hecho reportado exitosamente"),
            @OpenApiResponse(status = "404", description = "Hecho no encontrado"),
            @OpenApiResponse(status = "400", description = "Error al reportar hecho")
        }
    )
    public void reportarHecho(Context ctx) {
        try {
            String uuid = ctx.pathParam("uuid");
            this.serviceHecho.reportarHecho(uuid);
            ctx.status(200).result("Hecho reportado exitosamente");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrado")) {
                ctx.status(404).result(e.getMessage());
            } else {
                ctx.status(400).result("Error al reportar hecho: " + e.getMessage());
            }
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }
}
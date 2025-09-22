package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceHechoMetamapa;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dominio.criterios.CategoriaStrategy;
import utn.dds.dominio.criterios.TituloStrategy;
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
            // Obtener parámetros de filtros
            String categoria = ctx.queryParam("categoria");
            String titulo = ctx.queryParam("titulo");
            int pagina = ctx.queryParamAsClass("pagina", Integer.class).getOrDefault(0);
            int tamanioPagina = ctx.queryParamAsClass("tamanioPagina", Integer.class).getOrDefault(10);

            // Crear filtros basados en los parámetros
            List<HechoStrategy> filtros = new ArrayList<>();

            if (categoria != null && !categoria.isEmpty()) {
                filtros.add(new CategoriaStrategy(categoria));
            }

            if (titulo != null && !titulo.isEmpty()) {
                filtros.add(new TituloStrategy(titulo));
            }

            RespuestaPaginadaDTO<HechoDTO> respuesta = this.serviceHecho.obtenerHechosPaginados(filtros, pagina, tamanioPagina);
            ctx.json(respuesta);

        } catch (Exception e) {
            ctx.status(400).result("Error al buscar hechos: " + e.getMessage());
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
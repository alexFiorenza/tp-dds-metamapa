package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceColeccion;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dominio.criterios.CategoriaStrategy;
import utn.dds.dominio.criterios.TituloStrategy;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.HechoDTO;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
            Coleccion coleccion = this.serviceColeccion.obtenerColeccionPorId(identificador);

            if (coleccion != null) {
                ctx.json(coleccion); // Por ahora usar la entidad de dominio directamente
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
            @OpenApiParam(name = "categoria", description = "Filtrar por categoría"),
            @OpenApiParam(name = "titulo", description = "Filtrar por título")
        },
        responses = {
            @OpenApiResponse(status = "200", description = "Lista de hechos de la colección", content = @OpenApiContent(from = HechoDTO[].class)),
            @OpenApiResponse(status = "404", description = "Colección no encontrada"),
            @OpenApiResponse(status = "400", description = "Error al buscar hechos")
        }
    )
    public void obtenerHechosDeColeccion(Context ctx) {
        try {
            String identificador = ctx.pathParam("identificador");

            // Obtener parámetros de filtros
            String categoria = ctx.queryParam("categoria");
            String titulo = ctx.queryParam("titulo");

            // Crear filtros basados en los parámetros
            List<HechoStrategy> filtros = new ArrayList<>();

            if (categoria != null && !categoria.isEmpty()) {
                filtros.add(new CategoriaStrategy(categoria));
            }

            if (titulo != null && !titulo.isEmpty()) {
                filtros.add(new TituloStrategy(titulo));
            }

            List<Hecho> hechos = this.serviceColeccion.buscarHechosEnColeccion(identificador, filtros);
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());

            ctx.json(hechosDTO);

        } catch (RuntimeException e) {
            if (e.getMessage().contains("no encontrada")) {
                ctx.status(404).result(e.getMessage());
            } else {
                ctx.status(400).result("Error al buscar hechos: " + e.getMessage());
            }
        } catch (Exception e) {
            ctx.status(500).result("Error interno: " + e.getMessage());
        }
    }
}
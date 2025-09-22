package utn.dds.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.metamapa.service.ServiceColeccion;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.HechoDTO;

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
        responses = {
            @OpenApiResponse(status = "200", description = "Lista de colecciones", content = @OpenApiContent(from = ColeccionDTO[].class))
        }
    )
    public void obtenerColecciones(Context ctx) {
        List<Coleccion> colecciones = this.serviceColeccion.obtenerColecciones();
        List<ColeccionDTO> coleccionesDTO = colecciones.stream()
            .map(ColeccionDTO::fromColeccionBasic)
            .collect(Collectors.toList());
        ctx.json(coleccionesDTO);
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
        Coleccion coleccion = this.serviceColeccion.obtenerColeccionPorId(id);
        if (coleccion != null) {
            ColeccionDTO coleccionDTO = ColeccionDTO.fromColeccion(coleccion);
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
        requestBody = @OpenApiRequestBody(content = @OpenApiContent(from = Coleccion.class)),
        responses = {
            @OpenApiResponse(status = "201", description = "Colección creada exitosamente"),
            @OpenApiResponse(status = "400", description = "Error al crear colección")
        }
    )
    public void crearColeccion(Context ctx) {
        try {
            Coleccion nuevaColeccion = ctx.bodyAsClass(Coleccion.class);
            this.serviceColeccion.crearColeccion(nuevaColeccion);
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
        summary = "Buscar hechos en colección",
        operationId = "buscarHechosEnColeccion",
        path = "/administrador/coleccion/{id}/hechos",
        methods = HttpMethod.GET,
        tags = {"Administrador - Colecciones"},
        pathParams = @OpenApiParam(name = "id", description = "ID de la colección"),
        responses = {
            @OpenApiResponse(status = "200", description = "Lista de hechos encontrados", content = @OpenApiContent(from = HechoDTO[].class)),
            @OpenApiResponse(status = "400", description = "Error al buscar hechos")
        }
    )
    public void buscarHechosEnColeccion(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            List<HechoStrategy> filtros = ctx.bodyAsClass(List.class);
            List<Hecho> hechos = this.serviceColeccion.buscarHechosEnColeccion(id, filtros);
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            ctx.json(hechosDTO);
        } catch (Exception e) {
            ctx.status(400).result("Error al buscar hechos: " + e.getMessage());
        }
    }
}
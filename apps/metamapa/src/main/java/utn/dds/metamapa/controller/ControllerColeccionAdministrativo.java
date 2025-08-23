package utn.dds.metamapa.controller;

import io.javalin.http.Context;
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

    public void obtenerColecciones(Context ctx) {
        List<Coleccion> colecciones = this.serviceColeccion.obtenerColecciones();
        List<ColeccionDTO> coleccionesDTO = colecciones.stream()
            .map(ColeccionDTO::fromColeccionBasic)
            .collect(Collectors.toList());
        ctx.json(coleccionesDTO);
    }

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

    public void crearColeccion(Context ctx) {
        try {
            Coleccion nuevaColeccion = ctx.bodyAsClass(Coleccion.class);
            this.serviceColeccion.crearColeccion(nuevaColeccion);
            ctx.status(201).result("Colección creada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al crear colección: " + e.getMessage());
        }
    }

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

    public void eliminarColeccion(Context ctx) {
        try {
            String id = ctx.pathParam("id");
            this.serviceColeccion.eliminarColeccion(id);
            ctx.status(200).result("Colección eliminada exitosamente");
        } catch (Exception e) {
            ctx.status(400).result("Error al eliminar colección: " + e.getMessage());
        }
    }

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
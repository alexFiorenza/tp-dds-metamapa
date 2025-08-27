package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.agregador.service.ServiceRegistry;
import utn.dds.dto.FuenteDTO;
import java.util.UUID;

public class RegistryController {
    
    private ServiceRegistry serviceAgregador;
    
    public RegistryController(ServiceRegistry serviceAgregador) {
        this.serviceAgregador = serviceAgregador;
    }
    
    public void registrar(Context ctx) {
        try {
            FuenteDTO fuente = ctx.bodyAsClass(FuenteDTO.class);
            serviceAgregador.registrar(fuente);
            ctx.status(201).result("Fuente registrada exitosamente");
        } catch (IllegalArgumentException e) {
            ctx.status(400).result(e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error interno del servidor");
        }
    }
    
    public void obtenerFuentes(Context ctx) {
        try {
            ctx.json(serviceAgregador.obtenerTodasLasFuentes());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener las fuentes");
        }
    }
    
    public void obtenerFuentePorUrl(Context ctx) {
        try {
            String url = ctx.pathParam("url");
            FuenteDTO fuente = serviceAgregador.obtenerFuentePorUrl(url);
            if (fuente != null) {
                ctx.json(fuente);
            } else {
                ctx.status(404).result("Fuente no encontrada");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener la fuente");
        }
    }
    
    public void eliminarFuente(Context ctx) {
        try {
            String url = ctx.pathParam("url");
            boolean eliminada = serviceAgregador.eliminarFuente(url);
            if (eliminada) {
                ctx.status(200).result("Fuente eliminada exitosamente");
            } else {
                ctx.status(404).result("Fuente no encontrada");
            }
        } catch (Exception e) {
            ctx.status(500).result("Error al eliminar la fuente");
        }
    }
}

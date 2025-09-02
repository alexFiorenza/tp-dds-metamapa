package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.agregador.service.ServiceAgregador;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;

public class ControllerAgregador {
    
    private ServiceAgregador serviceAgregador;
    
    public ControllerAgregador(ServiceAgregador serviceAgregador) {
        this.serviceAgregador = serviceAgregador;
    }
    
    @OpenApi(
        summary = "Ejecutar proceso de agregación",
        operationId = "ejecutarAgregacion",
        path = "/agregacion",
        methods = HttpMethod.POST,
        tags = {"Agregación"},
        description = "Procesa todas las fuentes registradas y agrega nuevos hechos al repositorio",
        responses = {
            @OpenApiResponse(status = "200", description = "Agregación completada exitosamente"),
            @OpenApiResponse(status = "500", description = "Error durante el proceso de agregación")
        }
    )
    public void agregacion(Context ctx) {
        try {
            serviceAgregador.agregacion();
            ctx.status(200).result("Agregación completada exitosamente");
        } catch (Exception e) {
            ctx.status(500).result("Error durante el proceso de agregación: " + e.getMessage());
        }
    }
    
    @OpenApi(
        summary = "Obtener todos los hechos agregados",
        operationId = "obtenerHechos",
        path = "/hechos",
        methods = HttpMethod.GET,
        tags = {"Agregación"},
        description = "Obtiene la lista completa de hechos procesados y almacenados",
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Lista de hechos obtenida exitosamente",
                content = {@OpenApiContent(from = Hecho[].class)}
            ),
            @OpenApiResponse(status = "500", description = "Error al obtener los hechos")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            ctx.json(serviceAgregador.obtenerHechos());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener los hechos");
        }
    }
}
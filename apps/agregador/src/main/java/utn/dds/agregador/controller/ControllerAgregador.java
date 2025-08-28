package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.agregador.service.ServiceAgregador;

public class ControllerAgregador {
    
    private ServiceAgregador serviceAgregador;
    
    public ControllerAgregador(ServiceAgregador serviceAgregador) {
        this.serviceAgregador = serviceAgregador;
    }
    
    public void agregacion(Context ctx) {
        try {
            serviceAgregador.agregacion();
            ctx.status(200).result("Agregación completada exitosamente");
        } catch (Exception e) {
            ctx.status(500).result("Error durante el proceso de agregación: " + e.getMessage());
        }
    }
    
    public void obtenerHechos(Context ctx) {
        try {
            ctx.json(serviceAgregador.obtenerHechos());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener los hechos");
        }
    }
}
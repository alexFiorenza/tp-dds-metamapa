package utn.dds.fuentes.estatica;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;

import java.util.List;

public class ControllerFuenteEstatica {
    private final ServiceFuenteEstatica estaticaService;

    public ControllerFuenteEstatica(ServiceFuenteEstatica estaticaService) {
        this.estaticaService = estaticaService;
    }

    public void obtenerHechos(Context ctx) {
        try {
            List<Hecho> hechos = estaticaService.obtenerHechos();
            ctx.json(hechos);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }
}
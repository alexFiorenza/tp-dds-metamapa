package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;

import java.util.List;
import java.util.stream.Collectors;

public class ControllerHechoDinamica {
    private final ServiceHechoDinamica dinamicaService;

    public ControllerHechoDinamica(ServiceHechoDinamica dinamicaService) {
        this.dinamicaService = dinamicaService;
    }

    public void obtenerHechos(Context ctx) {
        try {
            List<Hecho> hechos = dinamicaService.obtenerHechos();
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            ctx.json(hechosDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }

    public void agregarHechos(Context ctx, List<HechoDTO> hechos) {
        try {
            dinamicaService.aportarHechos(hechos);
            ctx.json(hechos);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar hechos: " + e.getMessage());
        }
    }
}
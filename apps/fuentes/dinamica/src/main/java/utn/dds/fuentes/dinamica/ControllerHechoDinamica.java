package utn.dds.fuentes.dinamica;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;

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

    public void agregarHechos(Context ctx, List<Hecho> hechos) {
        try {
            dinamicaService.aportarHechos(hechos);
            ctx.json(hechos);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar hechos: " + e.getMessage());
        }
    }
}
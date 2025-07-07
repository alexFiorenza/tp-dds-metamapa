package utn.dds.fuentes.estatica.controller;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.estatica.service.ServiceFuenteEstatica;
import utn.dds.fuentes.estatica.service.model.strategies.ProcesadorStrategy;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerFuenteEstatica {
    private final ServiceFuenteEstatica estaticaService;

    public ControllerFuenteEstatica(String daoType, Map<String, Object> daoConfig, ProcesadorStrategy procesador) {
        this.estaticaService = new ServiceFuenteEstatica(daoType, daoConfig, procesador);
    }
    
    public void obtenerHechos(Context ctx) {
        try {
            List<Hecho> hechos = estaticaService.obtenerHechos();
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            ctx.json(hechosDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }
}
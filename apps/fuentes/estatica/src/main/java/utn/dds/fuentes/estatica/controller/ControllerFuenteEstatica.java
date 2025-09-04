package utn.dds.fuentes.estatica.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
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
    
    @OpenApi(
        summary = "Obtener hechos desde fuentes estáticas",
        operationId = "obtenerHechos",
        path = "/hechos",
        methods = HttpMethod.GET,
        tags = {"Fuentes Estáticas"},
        description = "Obtiene hechos desde archivos CSV. Si se especifica el parámetro 'path', procesará ese archivo específico. Si no, usará el archivo por defecto.",
        queryParams = {
            @OpenApiParam(
                name = "path",
                description = "Ruta específica del archivo CSV a procesar (ej: metamapa-data/tecnologicos/desastres_tecnologicos_argentina.csv)",
                required = false,
                type = String.class
            )
        },
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Lista de hechos obtenida exitosamente",
                content = {@OpenApiContent(from = HechoDTO[].class)}
            ),
            @OpenApiResponse(status = "500", description = "Error al procesar la fuente de datos")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            String path = ctx.queryParam("path");
            List<Hecho> hechos;
            
            if (path != null && !path.trim().isEmpty()) {
                hechos = estaticaService.obtenerHechos(path);
            } else {
                hechos = estaticaService.obtenerHechos();
            }
            
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            ctx.json(hechosDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }
}
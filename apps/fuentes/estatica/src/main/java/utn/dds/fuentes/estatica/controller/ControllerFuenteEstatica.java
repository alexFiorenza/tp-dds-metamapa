package utn.dds.fuentes.estatica.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
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
        description = "Obtiene hechos desde archivos CSV. Soporta paginación y path específico.",
        queryParams = {
            @OpenApiParam(
                name = "path",
                description = "Ruta específica del archivo CSV a procesar (ej: metamapa-data/tecnologicos/desastres_tecnologicos_argentina.csv)",
                required = false,
                type = String.class
            ),
            @OpenApiParam(
                name = "pagina",
                description = "Número de página (empezando desde 0). Si se especifica, se habilita la paginación",
                required = false,
                type = Integer.class
            ),
            @OpenApiParam(
                name = "tamanio",
                description = "Tamaño de página (máximo 100, por defecto 10)",
                required = false,
                type = Integer.class
            )
        },
        responses = {
            @OpenApiResponse(
                status = "200", 
                description = "Lista de hechos obtenida exitosamente. Si se usa paginación, retorna RespuestaPaginadaDTO",
                content = {@OpenApiContent(from = HechoDTO[].class)}
            ),
            @OpenApiResponse(status = "500", description = "Error al procesar la fuente de datos")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            String path = ctx.queryParam("path");
            String paginaParam = ctx.queryParam("pagina");
            String tamanioParam = ctx.queryParam("tamanio");
            
            // Siempre usar paginación, con valores por defecto si no se especifican
            int pagina = paginaParam != null ? Integer.parseInt(paginaParam) : 0;
            int tamanio = tamanioParam != null ? Integer.parseInt(tamanioParam) : 10;
            
            RespuestaPaginadaDTO<Hecho> respuestaPaginada = estaticaService.obtenerHechosPaginados(pagina, tamanio, path);
            
            // Convertir los hechos a DTO
            List<HechoDTO> hechosDTO = respuestaPaginada.getDatos().stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            
            // Crear respuesta paginada con DTOs
            RespuestaPaginadaDTO<HechoDTO> respuestaDTOPaginada = new RespuestaPaginadaDTO<>(
                hechosDTO,
                respuestaPaginada.getPagina(),
                respuestaPaginada.getTamanioPagina(),
                respuestaPaginada.getTotalElementos()
            );
            
            ctx.json(respuestaDTOPaginada);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Error en parámetros de paginación: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }
}
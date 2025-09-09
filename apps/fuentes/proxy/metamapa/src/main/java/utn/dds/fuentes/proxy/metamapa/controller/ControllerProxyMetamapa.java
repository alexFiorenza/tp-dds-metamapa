package utn.dds.fuentes.proxy.metamapa.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.proxy.metamapa.service.ServiceFuenteProxyMetaMapa;

import java.util.List;
import java.util.stream.Collectors;

public class ControllerProxyMetamapa {
    private final ServiceFuenteProxyMetaMapa proxyMetamapaService;

    public ControllerProxyMetamapa(String url) {
        this.proxyMetamapaService = new ServiceFuenteProxyMetaMapa(url);
    }

    @OpenApi(
        summary = "Obtener hechos desde proxy MetaMapa",
        operationId = "obtenerHechosProxy",
        path = "/hechos",
        methods = HttpMethod.GET,
        tags = {"Proxy MetaMapa"},
        description = "Obtiene hechos desde el servicio principal de MetaMapa a través del proxy. Soporta paginación con valores por defecto.",
        queryParams = {
            @OpenApiParam(
                name = "pagina",
                description = "Número de página (empezando desde 0). Por defecto 0",
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
                description = "Hechos obtenidos exitosamente con paginación",
                content = {@OpenApiContent(from = RespuestaPaginadaDTO.class)}
            ),
            @OpenApiResponse(status = "400", description = "Error en parámetros de paginación"),
            @OpenApiResponse(status = "500", description = "Error al comunicarse con MetaMapa")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            String paginaParam = ctx.queryParam("pagina");
            String tamanioParam = ctx.queryParam("tamanio");
            
            // Siempre usar paginación, con valores por defecto si no se especifican
            int pagina = paginaParam != null ? Integer.parseInt(paginaParam) : 0;
            int tamanio = tamanioParam != null ? Integer.parseInt(tamanioParam) : 10;
            
            RespuestaPaginadaDTO<Hecho> respuestaPaginada = proxyMetamapaService.obtenerHechosPaginados(pagina, tamanio);
            
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
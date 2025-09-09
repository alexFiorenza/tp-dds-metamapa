package utn.dds.fuentes.proxy.demo.controller;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.fuentes.proxy.demo.service.ServiceFuenteProxyDemo;
import utn.dds.fuentes.proxy.demo.service.model.conexion.ConexionExampleIml;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerFuenteProxyDemo {
    private final ServiceFuenteProxyDemo serviceFuenteProxyDemo;

    public ControllerFuenteProxyDemo(String daoType, Map<String, Object> daoConfig) throws MalformedURLException {
       this.serviceFuenteProxyDemo = new ServiceFuenteProxyDemo(new ConexionExampleIml(), daoType, daoConfig);
    }

    @OpenApi(
        summary = "Obtener hechos desde proxy demo",
        operationId = "obtenerHechosProxyDemo",
        path = "/hechos",
        methods = HttpMethod.GET,
        tags = {"Proxy Demo"},
        description = "Obtiene hechos desde el proxy demo para pruebas y desarrollo. Soporta paginación con valores por defecto.",
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
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            String paginaParam = ctx.queryParam("pagina");
            String tamanioParam = ctx.queryParam("tamanio");
            
            // Siempre usar paginación, con valores por defecto si no se especifican
            int pagina = paginaParam != null ? Integer.parseInt(paginaParam) : 0;
            int tamanio = tamanioParam != null ? Integer.parseInt(tamanioParam) : 10;
            
            RespuestaPaginadaDTO<Hecho> respuestaPaginada = serviceFuenteProxyDemo.obtenerHechosPaginados(pagina, tamanio);
            
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

    @OpenApi(
        summary = "Agregar hechos a proxy demo",
        operationId = "agregarHechosProxyDemo",
        path = "/hechos",
        methods = HttpMethod.PUT,
        tags = {"Proxy Demo"},
        description = "Agrega hechos al proxy demo para pruebas y desarrollo",
        responses = {
            @OpenApiResponse(status = "200", description = "Hechos agregados exitosamente"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void agregarHechos(Context ctx) {
        try {
            this.serviceFuenteProxyDemo.agregarHechos();
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar hechos: " + e.getMessage());
        }
    }
}

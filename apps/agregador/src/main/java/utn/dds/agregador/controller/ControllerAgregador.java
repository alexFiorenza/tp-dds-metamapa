package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.agregador.service.ServiceAgregador;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;
import utn.dds.dto.ResultadoAgregacionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

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
            @OpenApiResponse(
                status = "200", 
                description = "Agregación completada exitosamente con información detallada del proceso",
                content = {@OpenApiContent(from = ResultadoAgregacionDTO.class)}
            ),
            @OpenApiResponse(status = "500", description = "Error durante el proceso de agregación")
        }
    )
    public void agregacion(Context ctx) {
        try {
            ResultadoAgregacionDTO resultado = serviceAgregador.agregacion();
            ctx.status(200).json(resultado);
        } catch (Exception e) {
            ctx.status(500).result("Error durante el proceso de agregación: " + e.getMessage());
        }
    }
    
    @OpenApi(
        summary = "Obtener hechos agregados con paginación",
        operationId = "obtenerHechos",
        path = "/hechos",
        methods = HttpMethod.GET,
        tags = {"Agregación"},
        description = "Obtiene hechos procesados y almacenados con soporte de paginación. Si no se especifican parámetros de paginación, devuelve la primera página con 10 elementos.",
        queryParams = {
            @OpenApiParam(
                name = "pagina",
                description = "Número de página (comenzando desde 0). Por defecto: 0",
                required = false,
                type = Integer.class
            ),
            @OpenApiParam(
                name = "tamanioPagina", 
                description = "Cantidad de elementos por página (min: 1, max: 100). Por defecto: 10",
                required = false,
                type = Integer.class
            )
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "Hechos paginados obtenidos exitosamente",
                content = {@OpenApiContent(from = RespuestaPaginadaDTO.class)}
            ),
            @OpenApiResponse(status = "500", description = "Error al obtener los hechos")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            int pagina = ctx.queryParamAsClass("pagina", Integer.class).getOrDefault(0);
            int tamanioPagina = ctx.queryParamAsClass("tamanioPagina", Integer.class).getOrDefault(10);
            
            RespuestaPaginadaDTO<Hecho> respuesta = serviceAgregador.obtenerHechosPaginados(pagina, tamanioPagina);
            ctx.json(respuesta);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener los hechos: " + e.getMessage());
        }
    }
}
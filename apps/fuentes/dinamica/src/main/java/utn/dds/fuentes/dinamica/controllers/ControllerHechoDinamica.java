package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import io.javalin.openapi.*;
import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.Main;
import utn.dds.fuentes.dinamica.conexion.Conexion;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerHechoDinamica {
    private final ServiceHechoDinamica dinamicaService;
    private static final Logger loggerControllerDinamica = LoggerFactory.getLogger(ControllerHechoDinamica.class);

    // Hay que ver si esta bien implementada
    public ControllerHechoDinamica(String daoType, Map<String, Object> daoConfig) throws MalformedURLException {
        this.dinamicaService = new ServiceHechoDinamica(daoType, daoConfig);
    }

    @OpenApi(
        summary = "Obtener hechos desde fuentes dinámicas",
        operationId = "obtenerHechosDinamicos",
        path = "/hechos",
        methods = HttpMethod.GET,
        tags = {"Fuentes Dinámicas"},
        description = "Obtiene hechos desde fuentes dinámicas. Soporta paginación con valores por defecto.",
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
            @OpenApiResponse(status = "500", description = "Error al procesar la fuente de datos")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            loggerControllerDinamica.info("Obteniendo hechos....");
            String paginaParam = ctx.queryParam("pagina");
            String tamanioParam = ctx.queryParam("tamanio");
            
            // Siempre usar paginación, con valores por defecto si no se especifican
            int pagina = paginaParam != null ? Integer.parseInt(paginaParam) : 0;
            int tamanio = tamanioParam != null ? Integer.parseInt(tamanioParam) : 10;
            
            RespuestaPaginadaDTO<Hecho> respuestaPaginada = dinamicaService.obtenerHechosPaginados(pagina, tamanio);
            
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
        summary = "Agregar nuevo hecho a fuente dinámica",
        operationId = "agregarHechoDinamico",
        path = "/hechos",
        methods = HttpMethod.POST,
        tags = {"Fuentes Dinámicas"},
        description = "Agrega un nuevo hecho a la fuente dinámica",
        requestBody = @OpenApiRequestBody(
            description = "Datos del hecho a agregar",
            content = {@OpenApiContent(from = HechoDTO.class)}
        ),
        responses = {
            @OpenApiResponse(
                status = "201", 
                description = "Hecho agregado exitosamente",
                content = {@OpenApiContent(from = HechoDTO.class)}
            ),
            @OpenApiResponse(status = "400", description = "Datos del hecho inválidos"),
            @OpenApiResponse(status = "500", description = "Error interno del servidor")
        }
    )
    public void agregarHecho(Context ctx) {
        try {
            // convertir el hechoDTO a hecho normal
            loggerControllerDinamica.info("Agregando el hecho....");
            HechoDTO hechoDTO = ctx.bodyValidator(HechoDTO.class)
                    .get();
            Hecho hecho = hechoDTO.toHecho();   // Esto es por si lo usamos despues
            dinamicaService.aportarHecho(hechoDTO);
            ctx.status(201);
            ctx.json(hechoDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar el hecho: " + e.getMessage());
        }
    }
}
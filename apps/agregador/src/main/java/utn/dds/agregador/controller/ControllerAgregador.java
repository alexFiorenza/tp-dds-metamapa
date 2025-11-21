package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.agregador.service.ServiceAgregador;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.ResultadoAgregacionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.AgregacionJobDTO;
import utn.dds.agregador.dominio.AgregacionJob;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class ControllerAgregador {
    
    private ServiceAgregador serviceAgregador;
    
    public ControllerAgregador(ServiceAgregador serviceAgregador) {
        this.serviceAgregador = serviceAgregador;
    }
    
    @OpenApi(
        summary = "Iniciar proceso de agregación asíncrono",
        operationId = "ejecutarAgregacion",
        path = "/agregacion",
        methods = HttpMethod.POST,
        tags = {"Agregación"},
        description = "Inicia el proceso de agregación en segundo plano y retorna inmediatamente con un ID de job para consultar el estado",
        responses = {
            @OpenApiResponse(
                status = "202",
                description = "Agregación iniciada exitosamente. Use el jobId para consultar el estado",
                content = {@OpenApiContent(from = Map.class)}
            ),
            @OpenApiResponse(status = "500", description = "Error al iniciar el proceso de agregación")
        }
    )
    public void agregacion(Context ctx) {
        try {
            String jobId = serviceAgregador.iniciarAgregacionAsincrona();
            Map<String, Object> response = new HashMap<>();
            response.put("jobId", jobId);
            response.put("estado", "EN_PROGRESO");
            response.put("mensaje", "Agregación iniciada. Consulte el estado en GET /agregacion/" + jobId);
            ctx.status(202).json(response);
        } catch (Exception e) {
            ctx.status(500).result("Error al iniciar el proceso de agregación: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Listar agregaciones recientes",
        operationId = "listarAgregaciones",
        path = "/agregacion",
        methods = HttpMethod.GET,
        tags = {"Agregación"},
        description = "Lista las agregaciones más recientes",
        queryParams = {
            @OpenApiParam(
                name = "limit",
                description = "Cantidad máxima de jobs a retornar. Por defecto: 20",
                required = false,
                type = Integer.class
            )
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "Lista de agregaciones obtenida exitosamente",
                content = {@OpenApiContent(from = List.class)}
            ),
            @OpenApiResponse(status = "500", description = "Error al obtener las agregaciones")
        }
    )
    public void listarAgregaciones(Context ctx) {
        try {
            int limit = ctx.queryParamAsClass("limit", Integer.class).getOrDefault(20);

            // Validar límite
            if (limit > 100) {
                limit = 100;
            }
            if (limit < 1) {
                limit = 1;
            }

            List<AgregacionJob> jobs = serviceAgregador.obtenerJobsRecientes(limit);

            // Convertir a DTOs
            List<AgregacionJobDTO> dtos = jobs.stream()
                .map(job -> new AgregacionJobDTO(
                    job.getId(),
                    job.getEstado().toString(),
                    job.getFechaInicio(),
                    job.getFechaFin(),
                    job.getFuentesConsultadas(),
                    job.getTotalFuentes(),
                    job.getHechosObtenidos(),
                    job.getHechosAgregados(),
                    job.getErroresAsList(),
                    job.getMensajeError()
                ))
                .collect(java.util.stream.Collectors.toList());

            ctx.status(200).json(dtos);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener las agregaciones: " + e.getMessage());
        }
    }

    @OpenApi(
        summary = "Obtener estado de una agregación",
        operationId = "obtenerEstadoAgregacion",
        path = "/agregacion/{jobId}",
        methods = HttpMethod.GET,
        tags = {"Agregación"},
        description = "Consulta el estado actual de un job de agregación",
        pathParams = {
            @OpenApiParam(name = "jobId", description = "ID del job de agregación", required = true)
        },
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "Estado del job obtenido exitosamente",
                content = {@OpenApiContent(from = AgregacionJobDTO.class)}
            ),
            @OpenApiResponse(status = "404", description = "Job no encontrado"),
            @OpenApiResponse(status = "500", description = "Error al obtener el estado del job")
        }
    )
    public void obtenerEstadoAgregacion(Context ctx) {
        try {
            String jobId = ctx.pathParam("jobId");
            AgregacionJob job = serviceAgregador.obtenerEstadoJob(jobId);

            if (job == null) {
                ctx.status(404).result("Job no encontrado: " + jobId);
                return;
            }

            // Convertir a DTO
            AgregacionJobDTO dto = new AgregacionJobDTO(
                job.getId(),
                job.getEstado().toString(),
                job.getFechaInicio(),
                job.getFechaFin(),
                job.getFuentesConsultadas(),
                job.getTotalFuentes(),
                job.getHechosObtenidos(),
                job.getHechosAgregados(),
                job.getErroresAsList(),
                job.getMensajeError()
            );

            ctx.status(200).json(dto);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener el estado del job: " + e.getMessage());
        }
    }
    
    @OpenApi(
        summary = "Obtener hechos agregados con filtros y paginación",
        operationId = "obtenerHechos",
        path = "/hechos",
        methods = HttpMethod.GET,
        tags = {"Agregación"},
        description = "Obtiene hechos procesados y almacenados con soporte de filtros y paginación. Si no se especifican parámetros de paginación, devuelve la primera página con 10 elementos.",
        queryParams = {
            @OpenApiParam(name = "categoria", description = "Filtrar por categoría"),
            @OpenApiParam(name = "titulo", description = "Filtrar por título"),
            @OpenApiParam(name = "descripcion", description = "Filtrar por descripción"),
            @OpenApiParam(name = "origen", description = "Filtrar por origen"),
            @OpenApiParam(name = "fechaAcontecimiento", description = "Filtrar por fecha de acontecimiento (formato: YYYY-MM-DD)"),
            @OpenApiParam(name = "longitud", description = "Filtrar por longitud exacta", type = Double.class),
            @OpenApiParam(name = "latitud", description = "Filtrar por latitud exacta", type = Double.class),
            @OpenApiParam(name = "estado", description = "Filtrar por estado (ACTIVO, OCULTO)"),
            @OpenApiParam(name = "fechaCarga", description = "Filtrar por fecha de carga (formato: YYYY-MM-DDTHH:mm:ss)"),
            @OpenApiParam(name = "etiquetas", description = "Filtrar por etiquetas (separadas por comas)"),
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
            @OpenApiResponse(status = "400", description = "Error en parámetros de filtro"),
            @OpenApiResponse(status = "500", description = "Error al obtener los hechos")
        }
    )
    public void obtenerHechos(Context ctx) {
        try {
            // Parámetros de paginación
            int pagina = ctx.queryParamAsClass("pagina", Integer.class).getOrDefault(0);
            int tamanioPagina = ctx.queryParamAsClass("tamanioPagina", Integer.class).getOrDefault(10);

            // Crear filtros usando el Factory
            List<HechoStrategy> filtros = FiltroFactory.crearFiltros(ctx);

            RespuestaPaginadaDTO<HechoDTO> respuesta = serviceAgregador.obtenerHechos(filtros, pagina, tamanioPagina);
            ctx.json(respuesta);
        } catch (IllegalArgumentException e) {
            ctx.status(400).result("Error en parámetros de filtro: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener los hechos: " + e.getMessage());
        }
    }
}
package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.fuentes.TipoFuente;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.ArrayList;
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
        description = "Agrega un nuevo hecho a la fuente dinámica. Soporta subida de archivos multimedia usando multipart/form-data. " +
            "Los archivos se suben a S3/MinIO. Tipos permitidos: image/jpeg, image/png, image/gif, image/webp, video/mp4, video/mpeg, video/quicktime, video/webm. Tamaño máximo: 10MB. " +
            "El tipo del hecho se determina automáticamente según la presencia de archivos multimedia (MULTIMEDIA o TEXTO) y el origen se infiere como la fuente dinámica.",
        requestBody = @OpenApiRequestBody(
            description = "Multipart/form-data con campos: titulo, descripcion, categoria, longitud, latitud (requeridos). " +
                "Opcionales: fechaAcontecimiento, contribuyenteNombre, estado, etiquetas, multimedia (archivos). " +
                "Ejemplo cURL: curl -F 'titulo=...' -F 'multimedia=@imagen.jpg' http://localhost:7002/hechos",
            content = {@OpenApiContent(
                mimeType = "multipart/form-data",
                from = HechoDTO.class
            )}
        ),
        responses = {
            @OpenApiResponse(
                status = "201",
                description = "Hecho agregado exitosamente. URLs de multimedia generadas",
                content = {@OpenApiContent(from = HechoDTO.class)}
            ),
            @OpenApiResponse(status = "400", description = "Datos inválidos o archivo excede límites"),
            @OpenApiResponse(status = "500", description = "Error al subir multimedia a S3")
        }
    )
    public void agregarHecho(Context ctx) {
        try {
            loggerControllerDinamica.info("Agregando hecho con multipart/form-data...");

            // Parsear campos del formulario
            String titulo = ctx.formParam("titulo");
            String descripcion = ctx.formParam("descripcion");
            String categoria = ctx.formParam("categoria");
            String fechaAcontecimiento = ctx.formParam("fechaAcontecimiento");
            String contribuyenteNombre = ctx.formParam("contribuyenteNombre");
            String longitudStr = ctx.formParam("longitud");
            String latitudStr = ctx.formParam("latitud");
            String estadoStr = ctx.formParam("estado");
            String etiquetasStr = ctx.formParam("etiquetas");

            // Validar campos requeridos
            if (titulo == null || descripcion == null || categoria == null ||
                longitudStr == null || latitudStr == null) {
                ctx.status(400).result("Faltan campos requeridos: titulo, descripcion, categoria, longitud, latitud");
                return;
            }

            // Parsear tipo y coordenadas
            double longitud = Double.parseDouble(longitudStr);
            double latitud = Double.parseDouble(latitudStr);
            EstadoHecho estado = estadoStr != null ? EstadoHecho.valueOf(estadoStr.toUpperCase()) : EstadoHecho.ACTIVO;

            // Parsear etiquetas
            List<String> etiquetas = new ArrayList<>();
            if (etiquetasStr != null && !etiquetasStr.trim().isEmpty()) {
                etiquetas = List.of(etiquetasStr.split(","));
            }

            // Obtener archivos multimedia
            List<UploadedFile> archivos = ctx.uploadedFiles("multimedia");
            loggerControllerDinamica.info("Recibidos {} archivos multimedia", archivos.size());

            TipoHecho tipo = (archivos != null && !archivos.isEmpty()) ? TipoHecho.MULTIMEDIA : TipoHecho.TEXTO;
            String origen = TipoFuente.DINAMICA.name();

            // Crear HechoDTO
            HechoDTO hechoDTO = new HechoDTO(
                titulo,
                descripcion,
                categoria,
                fechaAcontecimiento,
                origen,
                contribuyenteNombre,
                tipo,
                longitud,
                latitud,
                null, // fechaCarga se asigna automáticamente
                estado,
                etiquetas,
                null, // uuid se genera automáticamente
                new ArrayList<>() // multimedia se llenará después
            );

            // Procesar y aportar el hecho con archivos
            HechoDTO resultado = dinamicaService.aportarHechoConArchivos(hechoDTO, archivos);

            loggerControllerDinamica.info("Hecho agregado exitosamente con {} archivos multimedia",
                resultado.getMultimedia() != null ? resultado.getMultimedia().size() : 0);

            ctx.status(201);
            ctx.json(resultado);

        } catch (IllegalArgumentException e) {
            // Errores de validación (tamaño, tipo MIME, campos inválidos)
            loggerControllerDinamica.warn("Error de validación al agregar hecho: {}", e.getMessage());
            ctx.status(400).result("Error de validación: " + e.getMessage());
        } catch (RuntimeException e) {
            // Errores de S3 u otros errores en runtime
            loggerControllerDinamica.error("Error al procesar multimedia: {}", e.getMessage(), e);
            ctx.status(500).result("Error al procesar multimedia: " + e.getMessage());
        } catch (Exception e) {
            loggerControllerDinamica.error("Error inesperado al agregar el hecho", e);
            ctx.status(500).result("Error al agregar el hecho: " + e.getMessage());
        }
    }
}
package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import io.javalin.http.UploadedFile;
import io.javalin.openapi.*;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.fuentes.TipoFuente;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.ContribuyenteDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import utn.dds.fuentes.dinamica.auth.ClerkTokenHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerHechoDinamica {
    private final ServiceHechoDinamica dinamicaService;
    private final ClerkTokenHelper clerkTokenHelper;
    private static final Logger loggerControllerDinamica = LoggerFactory.getLogger(ControllerHechoDinamica.class);

    // Hay que ver si esta bien implementada
    public ControllerHechoDinamica(String daoType, Map<String, Object> daoConfig) throws MalformedURLException {
        this.dinamicaService = new ServiceHechoDinamica(daoType, daoConfig);
        // Inicializar ClerkTokenHelper con la secret key desde variables de entorno
        String clerkSecretKey = System.getenv("CLERK_SECRET_KEY");
        this.clerkTokenHelper = new ClerkTokenHelper(clerkSecretKey);
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
            ContribuyenteDTO contribuyenteDTO = null;
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

            // Extraer información del usuario desde el token de Clerk si está presente
            String authHeader = ctx.header("Authorization");
            loggerControllerDinamica.info("Authorization header recibido: {}", authHeader != null ? "presente" : "ausente");
            
            if (authHeader != null && !authHeader.trim().isEmpty()) {
                var userInfoOpt = clerkTokenHelper.extractUserInfo(authHeader);
                if (userInfoOpt.isPresent()) {
                    var userInfo = userInfoOpt.get();
                    String contribuyenteId = userInfo.getUserId();
                    // Si no se proporcionó nombre del contribuyente, usar el del token
                    if (contribuyenteNombre == null || contribuyenteNombre.trim().isEmpty()) {
                        contribuyenteNombre = userInfo.getNombre();
                    }
                    contribuyenteDTO = new ContribuyenteDTO(contribuyenteNombre, contribuyenteId);
                    loggerControllerDinamica.info("Usuario identificado como contribuyente: {} ({})", contribuyenteNombre, contribuyenteId);
                    loggerControllerDinamica.info("ContribuyenteDTO creado: nombre={}, userId={}", contribuyenteDTO.getNombre(), contribuyenteDTO.getUserId());
                } else {
                    loggerControllerDinamica.warn("Token de Clerk presente pero no válido o sin información de usuario");
                }
            } else {
                loggerControllerDinamica.info("No se recibió header de Authorization");
            }

            // Crear HechoDTO
            HechoDTO hechoDTO = new HechoDTO(
                titulo,
                descripcion,
                categoria,
                fechaAcontecimiento,
                origen,
                contribuyenteDTO,
                tipo,
                longitud,
                latitud,
                null, // fechaCarga se asigna automáticamente
                estado,
                etiquetas,
                null, // uuid se genera automáticamente
                new ArrayList<>() // multimedia se llenará después
            );
            
            loggerControllerDinamica.info("HechoDTO creado con contribuyente: {}", hechoDTO.getContribuyente() != null ? 
                "nombre=" + hechoDTO.getContribuyente().getNombre() + ", userId=" + hechoDTO.getContribuyente().getUserId() : "null");

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

    @OpenApi(
        summary = "Actualizar hecho existente en fuente dinámica",
        operationId = "actualizarHechoDinamico",
        path = "/hechos/{uuid}",
        methods = HttpMethod.PATCH,
        tags = {"Fuentes Dinámicas"},
        description = "Actualiza un hecho existente en la fuente dinámica. Requiere autenticación y que el usuario sea el contribuyente del hecho. " +
            "Solo se puede editar dentro de una semana desde la creación. Soporta subida de archivos multimedia usando multipart/form-data.",
        pathParams = {
            @OpenApiParam(
                name = "uuid",
                description = "UUID del hecho a actualizar",
                required = true,
                type = String.class
            )
        },
        requestBody = @OpenApiRequestBody(
            description = "Multipart/form-data con campos opcionales: titulo, descripcion, categoria, longitud, latitud, " +
                "fechaAcontecimiento, etiquetas, multimedia (archivos). Solo se actualizarán los campos proporcionados.",
            content = {@OpenApiContent(
                mimeType = "multipart/form-data",
                from = HechoDTO.class
            )}
        ),
        responses = {
            @OpenApiResponse(
                status = "200",
                description = "Hecho actualizado exitosamente",
                content = {@OpenApiContent(from = HechoDTO.class)}
            ),
            @OpenApiResponse(status = "400", description = "Datos inválidos, archivo excede límites, o validación fallida"),
            @OpenApiResponse(status = "401", description = "No autenticado o token inválido"),
            @OpenApiResponse(status = "403", description = "No autorizado: no es el contribuyente del hecho"),
            @OpenApiResponse(status = "404", description = "Hecho no encontrado"),
            @OpenApiResponse(status = "410", description = "Hecho no puede ser editado: ha pasado más de una semana"),
            @OpenApiResponse(status = "500", description = "Error al procesar la actualización")
        }
    )
    public void actualizarHecho(Context ctx) {
        try {
            String uuid = ctx.pathParam("uuid");
            loggerControllerDinamica.info("Actualizando hecho con UUID: {}", uuid);

            // Validar que se proporcionó el UUID
            if (uuid == null || uuid.trim().isEmpty()) {
                ctx.status(400).result("UUID del hecho es requerido");
                return;
            }

            // Obtener el hecho existente
            HechoDTO hechoExistente;
            try {
                hechoExistente = dinamicaService.buscarHechoPorUuid(uuid);
            } catch (java.util.NoSuchElementException e) {
                ctx.status(404).result("Hecho no encontrado con UUID: " + uuid);
                return;
            }

            // Validar autenticación y que el usuario es el contribuyente
            String authHeader = ctx.header("Authorization");
            if (authHeader == null || authHeader.trim().isEmpty()) {
                ctx.status(401).result("Se requiere autenticación para actualizar un hecho");
                return;
            }

            var userInfoOpt = clerkTokenHelper.extractUserInfo(authHeader);
            if (!userInfoOpt.isPresent()) {
                ctx.status(401).result("Token de autenticación inválido");
                return;
            }

            var userInfo = userInfoOpt.get();
            String userId = userInfo.getUserId();

            // Validar que el usuario es el contribuyente del hecho
            if (hechoExistente.getContribuyente() == null || 
                hechoExistente.getContribuyente().getUserId() == null || 
                !hechoExistente.getContribuyente().getUserId().equals(userId)) {
                String contribuyenteId = hechoExistente.getContribuyente() != null ? 
                    hechoExistente.getContribuyente().getUserId() : null;
                loggerControllerDinamica.warn("Usuario {} intentó actualizar hecho {} que pertenece a {}", 
                    userId, uuid, contribuyenteId);
                ctx.status(403).result("No autorizado: solo el contribuyente del hecho puede editarlo");
                return;
            }

            // Validar que ha pasado menos de una semana desde la creación
            if (hechoExistente.getFechaCarga() != null && !hechoExistente.getFechaCarga().trim().isEmpty()) {
                try {
                    LocalDateTime fechaCarga = LocalDateTime.parse(
                        hechoExistente.getFechaCarga(), 
                        DateTimeFormatter.ISO_LOCAL_DATE_TIME
                    );
                    LocalDateTime ahora = LocalDateTime.now();
                    long diasTranscurridos = ChronoUnit.DAYS.between(fechaCarga, ahora);
                    
                    if (diasTranscurridos >= 7) {
                        loggerControllerDinamica.warn("Intento de editar hecho {} después de {} días (máximo: 7 días)", 
                            uuid, diasTranscurridos);
                        ctx.status(410).result("El hecho no puede ser editado: ha pasado más de una semana desde su creación");
                        return;
                    }
                } catch (Exception e) {
                    loggerControllerDinamica.error("Error al parsear fecha de carga: {}", hechoExistente.getFechaCarga(), e);
                    ctx.status(500).result("Error al validar fecha de creación del hecho");
                    return;
                }
            }

            // Parsear campos del formulario (solo los que se proporcionen)
            String titulo = ctx.formParam("titulo");
            String descripcion = ctx.formParam("descripcion");
            String categoria = ctx.formParam("categoria");
            String fechaAcontecimiento = ctx.formParam("fechaAcontecimiento");
            String longitudStr = ctx.formParam("longitud");
            String latitudStr = ctx.formParam("latitud");
            String estadoStr = ctx.formParam("estado");
            String etiquetasStr = ctx.formParam("etiquetas");

            // Actualizar solo los campos proporcionados
            if (titulo != null && !titulo.trim().isEmpty()) {
                hechoExistente.setTitulo(titulo);
            }
            if (descripcion != null && !descripcion.trim().isEmpty()) {
                hechoExistente.setDescripcion(descripcion);
            }
            if (categoria != null && !categoria.trim().isEmpty()) {
                hechoExistente.setCategoria(categoria);
            }
            if (fechaAcontecimiento != null && !fechaAcontecimiento.trim().isEmpty()) {
                hechoExistente.setFechaAcontecimiento(fechaAcontecimiento);
            }
            if (longitudStr != null && !longitudStr.trim().isEmpty()) {
                try {
                    hechoExistente.setLongitud(Double.parseDouble(longitudStr));
                } catch (NumberFormatException e) {
                    ctx.status(400).result("Longitud inválida: " + longitudStr);
                    return;
                }
            }
            if (latitudStr != null && !latitudStr.trim().isEmpty()) {
                try {
                    hechoExistente.setLatitud(Double.parseDouble(latitudStr));
                } catch (NumberFormatException e) {
                    ctx.status(400).result("Latitud inválida: " + latitudStr);
                    return;
                }
            }
            if (estadoStr != null && !estadoStr.trim().isEmpty()) {
                try {
                    hechoExistente.setEstado(EstadoHecho.valueOf(estadoStr.toUpperCase()));
                } catch (IllegalArgumentException e) {
                    ctx.status(400).result("Estado inválido: " + estadoStr);
                    return;
                }
            }
            if (etiquetasStr != null && !etiquetasStr.trim().isEmpty()) {
                List<String> etiquetas = List.of(etiquetasStr.split(","));
                hechoExistente.setEtiquetas(etiquetas);
            }

            // Obtener archivos multimedia (opcional)
            List<UploadedFile> archivos = ctx.uploadedFiles("multimedia");
            if (archivos != null && !archivos.isEmpty()) {
                loggerControllerDinamica.info("Recibidos {} archivos multimedia para actualización", archivos.size());
            }

            // Actualizar el tipo si se agregaron archivos multimedia
            if (archivos != null && !archivos.isEmpty()) {
                hechoExistente.setTipo(TipoHecho.MULTIMEDIA);
            }

            // Procesar y actualizar el hecho
            HechoDTO resultado = dinamicaService.actualizarHecho(hechoExistente, archivos);

            loggerControllerDinamica.info("Hecho {} actualizado exitosamente por usuario {}", uuid, userId);

            ctx.status(200);
            ctx.json(resultado);

        } catch (IllegalArgumentException e) {
            // Errores de validación (tamaño, tipo MIME, campos inválidos)
            loggerControllerDinamica.warn("Error de validación al actualizar hecho: {}", e.getMessage());
            ctx.status(400).result("Error de validación: " + e.getMessage());
        } catch (RuntimeException e) {
            // Errores de S3 u otros errores en runtime
            loggerControllerDinamica.error("Error al procesar actualización: {}", e.getMessage(), e);
            ctx.status(500).result("Error al procesar actualización: " + e.getMessage());
        } catch (Exception e) {
            loggerControllerDinamica.error("Error inesperado al actualizar el hecho", e);
            ctx.status(500).result("Error al actualizar el hecho: " + e.getMessage());
        }
    }
}
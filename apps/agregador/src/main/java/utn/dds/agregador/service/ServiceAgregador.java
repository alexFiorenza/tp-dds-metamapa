package utn.dds.agregador.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.agregador.persistencia.HechoRepository;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.EstrategiaDeteccionDuplicados;
import utn.dds.dominio.criterios.EstrategiaTituloUbicacionFecha;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.ResultadoAgregacionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceAgregador {

    private static final Logger logger = LoggerFactory.getLogger(ServiceAgregador.class);

    private HechoRepository hechoRepository;
    private ServiceRegistry serviceRegistry;
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    private EstrategiaDeteccionDuplicados estrategiaDeteccionDuplicados;
    private NormalizadorClient normalizadorClient;

    public ServiceAgregador(HechoRepository hechoRepository, ServiceRegistry serviceRegistry) {
        this.hechoRepository = hechoRepository;
        this.serviceRegistry = serviceRegistry;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.estrategiaDeteccionDuplicados = new EstrategiaTituloUbicacionFecha();

        // Inicializar cliente del normalizador desde variables de entorno
        String normalizadorUrl = System.getenv().getOrDefault("NORMALIZADOR_URL", "http://localhost:3005");
        boolean normalizadorHabilitado = Boolean.parseBoolean(
                System.getenv().getOrDefault("NORMALIZADOR_ENABLED", "true")
        );
        this.normalizadorClient = new NormalizadorClient(normalizadorUrl, normalizadorHabilitado);
    }
    
    private List<Hecho> filtrarDuplicados(List<Hecho> nuevosHechos) {
        List<Hecho> hechosExistentes = hechoRepository.findAll(); // Usar findAll para incluir todos los hechos
        List<Hecho> hechosSinDuplicados = new ArrayList<>();
        List<Hecho> hechosParaActualizar = new ArrayList<>();

        // Mapa para cachear contribuyentes existentes por userId
        Map<String, utn.dds.dominio.Contribuyente> contribuyentesCache = new HashMap<>();

        for (Hecho nuevoHecho : nuevosHechos) {
            // Resolver contribuyente existente antes de procesar el hecho
            if (nuevoHecho.getContribuyente() != null && nuevoHecho.getContribuyente().getUserId() != null) {
                String userId = nuevoHecho.getContribuyente().getUserId();

                // Buscar en cache o en base de datos
                utn.dds.dominio.Contribuyente contribuyenteExistente = contribuyentesCache.get(userId);
                if (contribuyenteExistente == null) {
                    // Buscar directamente en la base de datos usando el nuevo método
                    contribuyenteExistente = hechoRepository.findContribuyenteByUserId(userId);

                    if (contribuyenteExistente != null) {
                        contribuyentesCache.put(userId, contribuyenteExistente);
                        logger.info("Contribuyente existente encontrado para userId: {}", userId);
                    }
                }

                // Si encontramos un contribuyente existente, reutilizarlo
                if (contribuyenteExistente != null) {
                    nuevoHecho.setContribuyente(contribuyenteExistente);
                } else {
                    // Es un contribuyente nuevo, agregarlo al cache
                    contribuyentesCache.put(userId, nuevoHecho.getContribuyente());
                }
            }

            // Primero verificar si existe un hecho con el mismo UUID (actualización)
            Hecho hechoExistenteConMismoUuid = hechosExistentes.stream()
                .filter(h -> h.getUuid() != null && h.getUuid().equals(nuevoHecho.getUuid()))
                .findFirst()
                .orElse(null);

            if (hechoExistenteConMismoUuid != null) {
                // Es una actualización: actualizar el hecho existente con los nuevos datos
                actualizarHechoExistente(hechoExistenteConMismoUuid, nuevoHecho);
                hechosParaActualizar.add(hechoExistenteConMismoUuid);
                logger.info("Hecho con UUID {} será actualizado", nuevoHecho.getUuid());
                continue;
            }

            // Si no es actualización, verificar duplicados por contenido
            boolean esDuplicado = hechosExistentes.stream()
                .anyMatch(hechoExistente -> estrategiaDeteccionDuplicados.esDuplicado(nuevoHecho, hechoExistente));

            if (!esDuplicado) {
                // También verificar contra otros hechos ya agregados en esta sesión
                boolean esDuplicadoEnSesion = hechosSinDuplicados.stream()
                    .anyMatch(hechoEnSesion -> estrategiaDeteccionDuplicados.esDuplicado(nuevoHecho, hechoEnSesion));

                if (!esDuplicadoEnSesion) {
                    hechosSinDuplicados.add(nuevoHecho);
                }
            }
        }

        // Guardar actualizaciones
        if (!hechosParaActualizar.isEmpty()) {
            hechoRepository.saveAll(hechosParaActualizar);
            logger.info("Actualizados {} hechos existentes", hechosParaActualizar.size());
        }

        return hechosSinDuplicados;
    }
    
    /**
     * Actualiza un hecho existente con los datos de un nuevo hecho
     */
    private void actualizarHechoExistente(Hecho existente, Hecho nuevo) {
        existente.setTitulo(nuevo.getTitulo());
        existente.setDescripcion(nuevo.getDescripcion());
        existente.setCategoria(nuevo.getCategoria());
        existente.setFechaAcontecimiento(nuevo.getFechaAcontecimiento());
        existente.setLongitud(nuevo.getLongitud());
        existente.setLatitud(nuevo.getLatitud());
        existente.setTipo(nuevo.getTipo());
        existente.setEstado(nuevo.getEstado());
        existente.setEtiquetas(nuevo.getEtiquetas());
        existente.setMultimedia(nuevo.getMultimedia());
        existente.setFechaCarga(nuevo.getFechaCarga());
        
        // Actualizar contribuyente si el nuevo tiene uno
        if (nuevo.getContribuyente() != null) {
            existente.setContribuyente(nuevo.getContribuyente());
        }
    }
    
    private static final int NORMALIZADOR_BATCH_SIZE = 1000;

    public ResultadoAgregacionDTO agregacion() {
        LocalDateTime fechaEjecucion = LocalDateTime.now();
        List<FuenteDTO> fuentes = serviceRegistry.obtenerTodasLasFuentes();
        List<Hecho> hechosAgregados = new ArrayList<>();
        List<String> fuentesConError = new ArrayList<>();
        int hechosObtenidos = 0;

        for (FuenteDTO fuente : fuentes) {
            try {
                // Verificar si es fuente estática ya procesada
                if ("estatica".equals(fuente.getTipo())) {
                    Map<String, Object> metadata = fuente.getMetadata();
                    if (metadata != null && Boolean.TRUE.equals(metadata.get("procesado"))) {
                        logger.info("Saltando fuente estática ya procesada: {}", fuente.getHost());
                        continue;
                    }
                }

                String urlCompleta = construirUrlCompleta(fuente);
                List<Hecho> hechosDeEstaFuente = obtenerHechosDesdeFuente(urlCompleta, fuente);
                hechosObtenidos += hechosDeEstaFuente.size();
                hechosAgregados.addAll(hechosDeEstaFuente);

                // Actualizar metadata con timestamp y estado
                Map<String, Object> metadata = fuente.getMetadata();
                if (metadata == null) {
                    metadata = new HashMap<>();
                }

                // Actualizar timestamp de última consulta
                metadata.put("ultimaConsulta", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                // Marcar como procesado si es fuente estática
                if ("estatica".equals(fuente.getTipo())) {
                    metadata.put("procesado", true);
                    logger.info("Fuente estática marcada como procesada: {}", fuente.getHost());
                }

                fuente.setMetadata(metadata);
                serviceRegistry.actualizar(fuente);
                logger.info("Fuente consultada: {} - Hechos obtenidos: {}", fuente.getHost(), hechosDeEstaFuente.size());
            } catch (Exception e) {
                String errorMsg = fuente.getHost() + " - " + e.getMessage();
                fuentesConError.add(errorMsg);
                logger.error("Error al obtener datos de la fuente: {}", errorMsg, e);
            }
        }

        if (hechosAgregados.isEmpty()) {
            return new ResultadoAgregacionDTO(
                fuentes.size(),
                hechosObtenidos,
                0,
                fechaEjecucion,
                fuentesConError
            );
        }

        // Normalizar hechos antes de filtrar duplicados
        List<Hecho> hechosNormalizados = normalizarPorLotes(hechosAgregados);

        // Filtrar duplicados después de normalizar
        List<Hecho> hechosSinDuplicados = hechosNormalizados.isEmpty()
            ? List.of()
            : filtrarDuplicados(hechosNormalizados);

        // Guardar únicamente hechos sin duplicados (las actualizaciones ya se guardaron en filtrarDuplicados)
        if (!hechosSinDuplicados.isEmpty()) {
            hechoRepository.saveAll(hechosSinDuplicados);
        }

        return new ResultadoAgregacionDTO(
            fuentes.size(),
            hechosObtenidos,
            hechosSinDuplicados.size(),
            fechaEjecucion,
            fuentesConError
        );
    }
    
    private List<Hecho> obtenerHechosDesdeFuente(String url, FuenteDTO fuente) throws Exception {
        // Intentar primero con paginación, si falla usar método simple
        try {
            return obtenerHechosPaginadosDesdeFuente(url, fuente);
        } catch (Exception e) {
            // Si falla la paginación, intentar método simple
            logger.debug("Paginación no soportada para {}, usando método simple: {}", url, e.getMessage());
            return obtenerHechosSimpleDesdeFuente(url, fuente);
        }
    }
    
    private List<Hecho> obtenerHechosPaginadosDesdeFuente(String baseUrl, FuenteDTO fuente) throws Exception {
        List<Hecho> todosLosHechos = new ArrayList<>();
        int pagina = 0;
        int tamanioPagina = 50; // Tamaño de página para la consulta
        boolean hayMasPaginas = true;
        
        while (hayMasPaginas) {
            String urlPaginada = construirUrlConPaginacion(baseUrl, pagina, tamanioPagina);
            
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(urlPaginada))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json");
            
            // Agregar headers personalizados si existen
            if (fuente.getParams() != null && fuente.getParams().containsKey("headers")) {
                @SuppressWarnings("unchecked")
                Map<String, String> headers = (Map<String, String>) fuente.getParams().get("headers");
                headers.forEach(requestBuilder::header);
            }
            
            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Error HTTP: " + response.statusCode() + " para URL: " + urlPaginada);
            }
            
            String responseBody = response.body();
            
            try {
                // Intentar parsear como respuesta paginada
                @SuppressWarnings("unchecked")
                RespuestaPaginadaDTO<HechoDTO> respuestaPaginada = objectMapper.readValue(responseBody, 
                    new TypeReference<RespuestaPaginadaDTO<HechoDTO>>() {});
                
                // Verificar que realmente es una respuesta paginada válida
                if (respuestaPaginada.getDatos() == null) {
                    throw new RuntimeException("Respuesta no contiene estructura paginada válida");
                }
                
                // Convertir HechoDTOs a Hechos y sobrescribir origen
                for (HechoDTO dto : respuestaPaginada.getDatos()) {
                    Hecho hecho = dto.toHecho();
                    hecho.setOrigen(fuente.getUuid().toString());
                    todosLosHechos.add(hecho);
                }
                
                // Verificar si hay más páginas
                hayMasPaginas = respuestaPaginada.getDatos().size() == tamanioPagina 
                              && (pagina + 1) * tamanioPagina < respuestaPaginada.getTotalElementos();
                pagina++;
                
            } catch (Exception parseException) {
                // Si falla el parseo como respuesta paginada, la fuente no soporta paginación
                throw new RuntimeException("La fuente no soporta paginación: " + parseException.getMessage());
            }
        }
        
        return todosLosHechos;
    }
    
    private String construirUrlConPaginacion(String baseUrl, int pagina, int tamanioPagina) {
        String separador = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separador + "pagina=" + pagina + "&tamanio=" + tamanioPagina;
    }
    
    private List<Hecho> obtenerHechosSimpleDesdeFuente(String url, FuenteDTO fuente) throws Exception {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json");
        
        // Agregar headers personalizados si existen
        if (fuente.getParams() != null && fuente.getParams().containsKey("headers")) {
            @SuppressWarnings("unchecked")
            Map<String, String> headers = (Map<String, String>) fuente.getParams().get("headers");
            headers.forEach(requestBuilder::header);
        }
        
        // Determinar el método HTTP y body
        if (fuente.getParams() != null && fuente.getParams().containsKey("body")) {
            String body = (String) fuente.getParams().get("body");
            requestBuilder.POST(HttpRequest.BodyPublishers.ofString(body));
            requestBuilder.header("Content-Type", "application/json");
        } else {
            requestBuilder.GET();
        }
        
        HttpRequest request = requestBuilder.build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("Error HTTP: " + response.statusCode() + " para URL: " + url);
        }
        
        List<HechoDTO> hechosDTO = objectMapper.readValue(response.body(), new TypeReference<List<HechoDTO>>() {});
        
        List<Hecho> hechos = new ArrayList<>();
        for (HechoDTO dto : hechosDTO) {
            Hecho hecho = dto.toHecho();
            hecho.setOrigen(fuente.getUuid().toString());
            hechos.add(hecho);
        }

        return hechos;
    }
    
    private String construirUrlCompleta(FuenteDTO fuente) {
        StringBuilder urlBuilder = new StringBuilder(fuente.getHost());
        
        if (fuente.getParams() == null || fuente.getParams().isEmpty()) {
            return urlBuilder.toString();
        }
        
        // 1. Agregar endpoint si existe
        if (fuente.getParams().containsKey("endpoint")) {
            String endpoint = (String) fuente.getParams().get("endpoint");
            if (endpoint != null && !endpoint.trim().isEmpty()) {
                if (!endpoint.startsWith("/")) {
                    urlBuilder.append("/");
                }
                urlBuilder.append(endpoint);
            }
        }
        
        // 2. Agregar path parameters si existen
        if (fuente.getParams().containsKey("pathParams")) {
            @SuppressWarnings("unchecked")
            java.util.List<String> pathParams = (java.util.List<String>) fuente.getParams().get("pathParams");
            if (pathParams != null) {
                for (String param : pathParams) {
                    if (!urlBuilder.toString().endsWith("/")) {
                        urlBuilder.append("/");
                    }
                    urlBuilder.append(param);
                }
            }
        }
        
        // 3. Construir query parameters
        StringBuilder queryBuilder = new StringBuilder();
        
        // 3a. Query parameters explícitos
        if (fuente.getParams().containsKey("queryParams")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> queryParams = (Map<String, Object>) fuente.getParams().get("queryParams");
            if (queryParams != null) {
                for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                    if (queryBuilder.length() > 0) {
                        queryBuilder.append("&");
                    }
                    queryBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }
        
        // 3b. Parámetros legacy (compatibilidad hacia atrás)
        // Cualquier parámetro que no sea especial se convierte en query param
        for (Map.Entry<String, Object> entry : fuente.getParams().entrySet()) {
            String key = entry.getKey();
            if (!"endpoint".equals(key) && !"pathParams".equals(key) && 
                !"queryParams".equals(key) && !"headers".equals(key) && !"body".equals(key)) {
                
                if (queryBuilder.length() > 0) {
                    queryBuilder.append("&");
                }
                queryBuilder.append(key).append("=").append(entry.getValue());
            }
        }
        
        // 4. Agregar query string si hay parámetros
        if (queryBuilder.length() > 0) {
            urlBuilder.append("?").append(queryBuilder.toString());
        }
        
        return urlBuilder.toString();
    }
    
    /**
     * Obtiene hechos sin filtros (método de compatibilidad)
     */
    public RespuestaPaginadaDTO<Hecho> obtenerHechos(int pagina, int tamanioPagina) {
        return obtenerHechos(new ArrayList<>(), pagina, tamanioPagina);
    }

    /**
     * Obtiene hechos con filtros opcionales y paginación
     */
    public RespuestaPaginadaDTO<Hecho> obtenerHechos(List<HechoStrategy> filtros, int pagina, int tamanioPagina) {
        // Validaciones
        if (pagina < 0) {
            pagina = 0;
        }
        if (tamanioPagina <= 0) {
            tamanioPagina = 10; // Tamaño por defecto
        }
        if (tamanioPagina > 100) {
            tamanioPagina = 100; // Máximo 100 elementos por página
        }

        // Obtener todos los hechos
        List<Hecho> todosLosHechos = hechoRepository.find();

        // Aplicar filtros si existen
        List<Hecho> hechosFiltrados = todosLosHechos;
        if (filtros != null && !filtros.isEmpty()) {
            hechosFiltrados = new ArrayList<>();
            for (Hecho hecho : todosLosHechos) {
                boolean cumpleTodos = true;
                for (HechoStrategy filtro : filtros) {
                    if (!filtro.cumple(hecho)) {
                        cumpleTodos = false;
                        break;
                    }
                }
                if (cumpleTodos) {
                    hechosFiltrados.add(hecho);
                }
            }
        }

        long totalElementos = hechosFiltrados.size();

        // Calcular índices para la paginación
        int indiceInicio = pagina * tamanioPagina;
        int indiceFin = Math.min(indiceInicio + tamanioPagina, (int) totalElementos);

        // Obtener datos de la página actual
        List<Hecho> datosPagina;
        if (indiceInicio >= totalElementos) {
            datosPagina = new ArrayList<>();
        } else {
            datosPagina = hechosFiltrados.subList(indiceInicio, indiceFin);
        }

        return new RespuestaPaginadaDTO<>(datosPagina, pagina, tamanioPagina, totalElementos);
    }

    private List<Hecho> normalizarPorLotes(List<Hecho> hechos) {
        if (hechos == null || hechos.isEmpty()) {
            return List.of();
        }

        List<Hecho> hechosNormalizados = new ArrayList<>(hechos.size());

        for (int i = 0; i < hechos.size(); i += NORMALIZADOR_BATCH_SIZE) {
            int fin = Math.min(i + NORMALIZADOR_BATCH_SIZE, hechos.size());
            List<Hecho> lote = new ArrayList<>(hechos.subList(i, fin));
            List<Hecho> loteNormalizado = normalizadorClient.normalizar(lote);

            if (loteNormalizado != null && !loteNormalizado.isEmpty()) {
                hechosNormalizados.addAll(loteNormalizado);
            }
        }

        return hechosNormalizados;
    }
}
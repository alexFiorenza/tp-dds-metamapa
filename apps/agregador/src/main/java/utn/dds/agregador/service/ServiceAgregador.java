package utn.dds.agregador.service;

import utn.dds.agregador.persistencia.HechoRepository;
import utn.dds.dominio.Hecho;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.HechoDTO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ServiceAgregador {
    
    private HechoRepository hechoRepository;
    private ServiceRegistry serviceRegistry;
    private HttpClient httpClient;
    private ObjectMapper objectMapper;
    
    public ServiceAgregador(HechoRepository hechoRepository, ServiceRegistry serviceRegistry) {
        this.hechoRepository = hechoRepository;
        this.serviceRegistry = serviceRegistry;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }
    
    public void agregacion() {
        List<FuenteDTO> fuentes = serviceRegistry.obtenerTodasLasFuentes();
        List<Hecho> hechosAgregados = new ArrayList<>();
        
        for (FuenteDTO fuente : fuentes) {
            try {
                String urlCompleta = construirUrlCompleta(fuente);
                List<Hecho> hechosDeEstaFuente = obtenerHechosDesdeFuente(urlCompleta, fuente);
                hechosAgregados.addAll(hechosDeEstaFuente);
            } catch (Exception e) {
                System.err.println("Error al obtener datos de la fuente: " + fuente.getHost() + " - " + e.getMessage());
            }
        }
        
        hechoRepository.saveAll(hechosAgregados);
    }
    
    private List<Hecho> obtenerHechosDesdeFuente(String url, FuenteDTO fuente) throws Exception {
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
            hechos.add(dto.toHecho());
        }
        
        return hechos;
    }
    
    private String construirUrlCompleta(FuenteDTO fuente) {
        StringBuilder urlBuilder = new StringBuilder(fuente.getHost());
        
        if (fuente.getParams() != null && !fuente.getParams().isEmpty()) {
            // Manejar path parameters
            if (fuente.getParams().containsKey("path")) {
                String path = (String) fuente.getParams().get("path");
                if (!path.startsWith("/")) {
                    urlBuilder.append("/");
                }
                urlBuilder.append(path);
            }
            
            // Manejar query parameters
            StringBuilder queryBuilder = new StringBuilder();
            fuente.getParams().entrySet().forEach(entry -> {
                if (!"path".equals(entry.getKey()) && !"headers".equals(entry.getKey()) && !"body".equals(entry.getKey())) {
                    if (queryBuilder.length() > 0) {
                        queryBuilder.append("&");
                    }
                    queryBuilder.append(entry.getKey()).append("=").append(entry.getValue());
                }
            });
            
            if (queryBuilder.length() > 0) {
                urlBuilder.append("?").append(queryBuilder.toString());
            }
        }
        
        return urlBuilder.toString();
    }
    
    public List<Hecho> obtenerHechos() {
        return hechoRepository.find();
    }
}
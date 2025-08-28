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
                List<Hecho> hechosDeEstaFuente = obtenerHechosDesdeFuente(fuente.getUrl());
                hechosAgregados.addAll(hechosDeEstaFuente);
            } catch (Exception e) {
                System.err.println("Error al obtener datos de la fuente: " + fuente.getUrl() + " - " + e.getMessage());
            }
        }
        
        hechoRepository.saveAll(hechosAgregados);
    }
    
    private List<Hecho> obtenerHechosDesdeFuente(String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .GET()
                .build();
        
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
    
    public List<Hecho> obtenerHechos() {
        return hechoRepository.find();
    }
}
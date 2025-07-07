package utn.dds.fuentes.proxy.demo.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.Redis;
import utn.dds.dominio.Hecho;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RepositoryFuenteProxyDemo {
    private final Redis<String> redisClient;
    private final ObjectMapper objectMapper;
    private final String CACHE_KEY = "fuente_proxy_demo_hechos";
    private final String ULTIMA_EJECUCION_KEY = "fuente_proxy_demo_ultima_ejecucion";
    
    public RepositoryFuenteProxyDemo(IDAO<String> dao) {
        this.redisClient = (Redis<String>) dao;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Hecho> obtenerHechos() {
        try {
            String hechosJson = redisClient.findById(CACHE_KEY);
            if (hechosJson != null) {
                return objectMapper.readValue(hechosJson, new TypeReference<List<Hecho>>() {});
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar hechos desde cache", e);
        }
        return List.of();
    }

    public void guardarHechos(List<Hecho> hechos) {
        try {
            String hechosJson = objectMapper.writeValueAsString(hechos);
            redisClient.save(CACHE_KEY, hechosJson);
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar hechos para cache", e);
        }
    }

    public LocalDateTime obtenerUltimaEjecucion() {
        String ultimaEjecucionStr = redisClient.findById(ULTIMA_EJECUCION_KEY);
        if (ultimaEjecucionStr != null) {
            return LocalDateTime.parse(ultimaEjecucionStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return null;
    }

    public void guardarUltimaEjecucion(LocalDateTime ultimaEjecucion) {
        redisClient.save(ULTIMA_EJECUCION_KEY, ultimaEjecucion.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}

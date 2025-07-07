package utn.dds.fuentes.proxy.demo.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.Redis;
import utn.dds.dominio.Hecho;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.List;

public class HechoRepository {
    private final Redis<String> redisClient;
    private final ObjectMapper objectMapper;
    private final String CACHE_KEY = "fuente_proxy_demo_hechos";
    
    public HechoRepository(IDAO<String> dao) {
        this.redisClient = (Redis<String>) dao;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Hecho> obtener() {
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

    private void guardar(List<Hecho> hechos) {
        try {
            String hechosJson = objectMapper.writeValueAsString(hechos);
            redisClient.save(CACHE_KEY, hechosJson);
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar hechos para cache", e);
        }
    }

    public List<Hecho> actualizar(List<Hecho> hechos){
        List<Hecho> hechosCache = this.obtener();
        // Agrego los nuevos hechos a la cache
        hechosCache.addAll(hechos);
        // Actualizo la cache
        this.guardar(hechosCache);
        // Devuelvo los nuevos hechos
        return hechosCache;
    }
}
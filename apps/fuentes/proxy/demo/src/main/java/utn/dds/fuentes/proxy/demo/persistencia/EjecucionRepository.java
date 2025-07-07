package utn.dds.fuentes.proxy.demo.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class EjecucionRepository {
    private final IDAO<Object> dao;
    private final ObjectMapper objectMapper;
    
    public EjecucionRepository(String daoType, Map<String, Object> daoConfig) {
        if ("filesystem".equals(daoType)) {
            Map<String, Object> config = new java.util.HashMap<>();
            config.put("url", "mocks/ultimaEjecucion.json");
            this.dao = DAOFactory.createDAO(Object.class, daoType, config);
        } else {
            this.dao = DAOFactory.createDAO(Object.class, daoType, daoConfig);
        }
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public LocalDateTime obtenerUltimaEjecucion() {
        try {
            List<Object> data = dao.find();
            if (!data.isEmpty() && data.get(0) instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) data.get(0);
                String ejecucionStr = (String) map.get("ejecucion");
                if (ejecucionStr != null) {
                    return LocalDateTime.parse(ejecucionStr);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener última ejecución", e);
        }
        return LocalDateTime.now().minusDays(1);
    }

    public void guardarUltimaEjecucion(LocalDateTime ultimaEjecucion) {
        // Para este caso de uso mock, no necesitamos guardar
        // Los datos están en el archivo JSON estático
    }
}
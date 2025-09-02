package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.dto.FuenteDTO;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;

public class FuentesRepository {
    
    private IDAO<FuenteDTO> dao;
    
    public FuentesRepository() {
        // Constructor por defecto que usa configuración por defecto
        this("filesystem", new HashMap<>());
    }
    
    public FuentesRepository(String daoType, Map<String, Object> daoConfig) {
        if ("filesystem".equals(daoType)) {
            // Para filesystem, usar configuración específica
            Map<String, Object> config = new HashMap<>();
            config.put("url", "mocks/fuentes.json");
            this.dao = DAOFactory.createDAO(FuenteDTO.class, daoType, config);
        } else {
            // Para otros tipos de DAO, usar la configuración provista
            this.dao = DAOFactory.createDAO(FuenteDTO.class, daoType, daoConfig);
        }
    }
    
    public List<FuenteDTO> find() {
        return dao.find();
    }
    
    public void save(FuenteDTO fuente) {
        if (fuente.getUuid() == null) {
            fuente.setUuid(UUID.randomUUID());
        }
        
        // Para el repositorio de fuentes, necesitamos manejar la lista completa
        // ya que el DAO no tiene métodos específicos para búsqueda por campo
        List<FuenteDTO> fuentes = dao.find();
        
        // Verificar si ya existe una fuente con el mismo host
        boolean exists = fuentes.stream().anyMatch(f -> f.getHost().equals(fuente.getHost()));
        if (!exists) {
            fuentes.add(fuente);
            dao.saveAll(fuentes);
        }
    }
    
    public FuenteDTO findByHost(String host) {
        return dao.find().stream()
                .filter(f -> f.getHost().equals(host))
                .findFirst()
                .orElse(null);
    }
    
    public boolean removeByHost(String host) {
        List<FuenteDTO> fuentes = dao.find();
        boolean removed = fuentes.removeIf(f -> f.getHost().equals(host));
        if (removed) {
            dao.saveAll(fuentes);
        }
        return removed;
    }
    
    public boolean removeByUuid(UUID uuid) {
        List<FuenteDTO> fuentes = dao.find();
        boolean removed = fuentes.removeIf(f -> f.getUuid().equals(uuid));
        if (removed) {
            dao.saveAll(fuentes);
        }
        return removed;
    }
    
    public FuenteDTO findByUuid(UUID uuid) {
        return dao.find().stream()
                .filter(f -> f.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }
}
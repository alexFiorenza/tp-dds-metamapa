package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.dto.HechoDTO;
import utn.dds.dominio.Hecho;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class HechoRepository {
    
    private IDAO<HechoDTO> dao;
    
    public HechoRepository() {
        // Constructor por defecto que usa configuración por defecto
        this("filesystem", new HashMap<>());
    }
    
    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
        if ("filesystem".equals(daoType)) {
            // Para filesystem, usar configuración específica
            Map<String, Object> config = new HashMap<>();
            config.put("url", "src/main/resources/mocks/hechos.json");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, config);
        } else {
            // Para otros tipos de DAO, usar la configuración provista
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, daoConfig);
        }
    }
    
    public List<Hecho> find() {
        List<HechoDTO> hechosDTO = dao.find();
        return hechosDTO.stream()
                .map(HechoDTO::toHecho)
                .collect(Collectors.toList());
    }
    
    public void save(Hecho hecho) {
        // Convertir Hecho a HechoDTO y agregarlo a la lista
        List<HechoDTO> hechosDTO = dao.find();
        hechosDTO.add(HechoDTO.fromHecho(hecho));
        dao.saveAll(hechosDTO);
    }
    
    public void saveAll(List<Hecho> hechos) {
        // Obtener hechos existentes y agregar los nuevos
        List<HechoDTO> hechosDTO = dao.find();
        List<HechoDTO> nuevosHechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
        hechosDTO.addAll(nuevosHechosDTO);
        dao.saveAll(hechosDTO);
    }
}
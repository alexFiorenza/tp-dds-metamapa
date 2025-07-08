package utn.dds.fuentes.proxy.demo.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HechoRepository {
    private final IDAO<HechoDTO> dao;
    
    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
        if ("filesystem".equals(daoType)) {
            Map<String, Object> config = new java.util.HashMap<>();
            config.put("url", "mocks/hechos.json");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, config);
        } else {
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, daoConfig);
        }
    }
    
    public HechoRepository(IDAO<HechoDTO> dao) {
        this.dao = dao;
    }

    public List<Hecho> obtener() {
        List<HechoDTO> hechosDTO = dao.find();
        return hechosDTO.stream()
                .map(HechoDTO::toHecho)
                .collect(Collectors.toList());
    }

    public List<Hecho> actualizar(List<Hecho> hechos){
        // Para este caso de uso mock, no necesitamos actualizar
        // Los datos están en el archivo JSON estático
        return this.obtener();
    }
}
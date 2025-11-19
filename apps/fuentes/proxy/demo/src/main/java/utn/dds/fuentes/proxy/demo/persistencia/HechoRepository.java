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
    private final String daoType;

    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
        this.daoType = daoType;
        if ("filesystem".equals(daoType)) {
            Map<String, Object> config = new java.util.HashMap<>();
            config.put("url", "src/main/resources/mocks/hechos.json");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, config);
        } else if ("mongodb".equals(daoType)) {
            // Para MongoDB, especificar la colección para hechos
            Map<String, Object> configHechos = new java.util.HashMap<>(daoConfig);
            configHechos.put("collection", "hechos");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, configHechos);
        } else {
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, daoConfig);
        }
    }

    public HechoRepository(IDAO<HechoDTO> dao) {
        this.dao = dao;
        this.daoType = "custom";
    }

    public List<Hecho> obtener() {
        List<HechoDTO> hechosDTO = dao.find();
        return hechosDTO.stream()
                .map(HechoDTO::toHecho)
                .collect(Collectors.toList());
    }

    public List<Hecho> actualizar(List<Hecho> hechos){
        // Para filesystem (mock), no persistimos cambios
        if ("filesystem".equals(daoType)) {
            return this.obtener();
        }

        // Para MongoDB u otros DAOs, guardamos los hechos
        List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());

        dao.saveAll(hechosDTO);
        return hechos;
    }
}
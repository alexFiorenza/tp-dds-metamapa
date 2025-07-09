package utn.dds.fuentes.dinamica.repositories;

import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class HechoRepository {
    private final IDAO<HechoDTO> dao;

    // Esto es una copia del Repository de la proxy demo, hay que cambiarle los valores

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

    public List<Hecho> obtenerHechos() {
        List<HechoDTO> hechosDTO = dao.find();
        return hechosDTO.stream()
                .map(HechoDTO::toHecho)
                .collect(Collectors.toList());
    }

    // Esta creo que no iria
    public List<Hecho> actualizar(List<Hecho> hechos){
        // Para este caso de uso mock, no necesitamos actualizar
        // Los datos están en el archivo JSON estático
        return this.obtenerHechos();
    }

    public List<Hecho> aportarHechos(List<Hecho> hechos) throws IOException {
        return null;
    }
} 
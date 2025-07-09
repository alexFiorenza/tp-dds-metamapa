package utn.dds.fuentes.dinamica.repositories;

import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;

import java.io.IOException;
import java.util.ArrayList;
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

    // A chequear si es Hecho o HechoDTO
    public List<Hecho> aportarHechos(List<HechoDTO> hechosDTO) throws IOException {;
        List<Hecho> hechos = new ArrayList<>();
        for (HechoDTO dto : hechosDTO) {
            Hecho hecho = dto.toHecho();
            dao.save(dto);
            hechos.add(hecho);
        }
        return hechos;
    }

    public Hecho cambiarEstado(Hecho hecho) throws IOException {
        if(hecho.getEstado()== EstadoHecho.ACTIVO){
            hecho.ocultar();
        } else { hecho.activar(); }
        return hecho;
    }
} 
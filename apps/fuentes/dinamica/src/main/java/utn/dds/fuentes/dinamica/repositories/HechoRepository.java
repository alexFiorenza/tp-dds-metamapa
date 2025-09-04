package utn.dds.fuentes.dinamica.repositories;

import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.fuentes.dinamica.Main;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HechoRepository {
    private final IDAO<HechoDTO> dao;
    private static final Logger loggerRepository = LoggerFactory.getLogger(HechoRepository.class);

    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
        if ("filesystem".equals(daoType)) {
            //loggerRepository.info("Estoy dentro de HechoRepository");  -- Logger comentado para debuggeo.
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

    public List<Hecho> obtenerHechos() throws IOException {
        //
        List<HechoDTO> hechosDTO = dao.find();
        List<Hecho> hechos = hechosDTO.stream()
                .map(HechoDTO::toHecho)
                .collect(Collectors.toList());
        return hechos;
    }

    public Hecho buscarHecho(String titulo) throws IOException {
        List<Hecho> hechos = obtenerHechos();

        Optional<Hecho> encontrado = hechos.stream()
                .filter(s -> s.getTitulo().equals(titulo))
                .findFirst();

        if (encontrado.isPresent()) {
            Hecho hechoEncontrado = encontrado.get();
            return hechoEncontrado;
        } else {
            throw new NoSuchElementException("No se encontró el Hecho con titulo: " + titulo);
        }
    }

    // Falta toquetear esta
    public HechoDTO aportarHecho(HechoDTO hecho) throws IOException {;
        dao.save(hecho);   // Aca no se porque se guardaria
        return hecho;
    }

    public Hecho cambiarEstado(Hecho hecho) throws IOException {
        if(hecho.getEstado()== EstadoHecho.ACTIVO){
            hecho.ocultar();
        } else { hecho.activar(); }
        return hecho;
    }
} 
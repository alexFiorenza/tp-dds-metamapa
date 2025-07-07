package utn.dds.fuentes.estatica.service;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.fuentes.estatica.persistencia.HechoRepository;
import utn.dds.fuentes.estatica.service.model.FuenteEstaticaImpl;
import utn.dds.fuentes.estatica.service.model.strategies.ProcesadorStrategy;
import utn.dds.daos.IDAO;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class ServiceFuenteEstatica {
    private final HechoRepository hechoRepository;
    private final ProcesadorStrategy procesador;
    
    public ServiceFuenteEstatica(String daoType, Map<String, Object> daoConfig, ProcesadorStrategy procesador) {
        this.hechoRepository = new HechoRepository(daoType, daoConfig);
        this.procesador = procesador;
    }
    
    public ServiceFuenteEstatica(IDAO<Hecho> dao, ProcesadorStrategy procesador) {
        this.hechoRepository = new HechoRepository(dao);
        this.procesador = procesador;
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        InputStream data = hechoRepository.leerArchivo();
        FuenteDeDatos fuente = new FuenteEstaticaImpl(data, procesador);
        return fuente.obtenerHechos();
    }
}
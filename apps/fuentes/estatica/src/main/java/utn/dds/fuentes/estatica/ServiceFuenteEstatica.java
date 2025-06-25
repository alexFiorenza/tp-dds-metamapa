package utn.dds.fuentes.estatica;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.estatica.strategies.ProcesadorStrategy;
import utn.dds.repository.IDAO;

import java.io.IOException;
import java.util.List;

public class ServiceFuenteEstatica {
    private final EstaticaRepository repository;
    
    public ServiceFuenteEstatica(IDAO<Hecho> dao, ProcesadorStrategy procesador) {
        this.repository = new EstaticaRepository(dao, procesador);
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        return repository.obtenerHechos();
    }
}
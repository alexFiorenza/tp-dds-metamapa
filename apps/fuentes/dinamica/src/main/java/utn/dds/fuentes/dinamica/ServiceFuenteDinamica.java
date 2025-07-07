package utn.dds.fuentes.dinamica;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;

import java.io.IOException;
import java.util.List;

public class ServiceFuenteDinamica {
    private final DinamicaRepository repository;
    
    public ServiceFuenteDinamica(IDAO<Hecho> dao) {
        this.repository = new DinamicaRepository(dao);
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        return repository.obtenerHechos();
    }

    public List<Hecho> aportarHechos(List<Hecho> hechos) throws IOException {
        return repository.aportarHechos(hechos);
    }
}
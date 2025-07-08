package utn.dds.fuentes.dinamica;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.FuenteDinamica;

import java.io.IOException;
import java.util.List;

public class ServiceHechoDinamica {
    private final HechoRepository repository;
    private final FuenteDinamica fuenteDeDatos;
    
    public ServiceHechoDinamica(IDAO<Hecho> dao, FuenteDinamica fuenteDeDatos) {
        this.repository = new HechoRepository(dao);
        this.fuenteDeDatos = fuenteDeDatos;
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        return repository.obtenerHechos();
    }

    public List<Hecho> aportarHechos(List<Hecho> hechos) throws IOException {
        return repository.aportarHechos(hechos);
    }
}
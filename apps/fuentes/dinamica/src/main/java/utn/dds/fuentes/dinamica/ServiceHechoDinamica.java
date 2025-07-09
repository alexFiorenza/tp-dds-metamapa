package utn.dds.fuentes.dinamica;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;

import java.io.IOException;
import java.util.List;

public class ServiceHechoDinamica {
    private final HechoRepository repository;
    private final FuenteDinamicaImpl fuenteDeDatos;
    
    public ServiceHechoDinamica(IDAO<Hecho> dao, FuenteDinamicaImpl fuenteDeDatos) {
        this.repository = new HechoRepository(dao);   // Aca no se porque falla si en teoria permite ue solo se le envie el dao
        this.fuenteDeDatos = fuenteDeDatos;
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        return repository.obtenerHechos();
    }

    public List<Hecho> aportarHechos(List<Hecho> hechos) throws IOException {
        return repository.aportarHechos(hechos);
    }
}
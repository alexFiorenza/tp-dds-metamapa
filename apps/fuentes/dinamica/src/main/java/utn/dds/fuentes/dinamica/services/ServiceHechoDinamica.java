package utn.dds.fuentes.dinamica.services;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.repositories.HechoRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class ServiceHechoDinamica {
    private final HechoRepository repository;
    private final FuenteDinamicaImpl fuenteDeDatos;
    
    public ServiceHechoDinamica(IDAO<Hecho> dao, FuenteDinamicaImpl fuenteDeDatos) {
        this.repository = new HechoRepository(dao);
        this.fuenteDeDatos = fuenteDeDatos;
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        return repository.obtenerHechos();
    }

    public Hecho aportarHecho(Hecho hecho) throws IOException {
        return repository.aportarHecho(hecho);
    }

    public Hecho cambiarEstado(Hecho hecho) throws IOException {
        return repository.cambiarEstado(hecho);
    }
}
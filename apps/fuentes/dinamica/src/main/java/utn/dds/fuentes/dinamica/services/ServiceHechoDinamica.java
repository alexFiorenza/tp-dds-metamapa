package utn.dds.fuentes.dinamica.services;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.repositories.HechoRepository;

import java.io.IOException;
import java.util.List;

public class ServiceHechoDinamica {
    private final HechoRepository repository;
    private final FuenteDinamicaImpl fuenteDeDatos;
    
    public ServiceHechoDinamica(IDAO<HechoDTO> dao, FuenteDinamicaImpl fuenteDeDatos) {
        this.repository = new HechoRepository(dao);   // Aca no se porque falla si en teoria permite que solo se le envie el dao
        this.fuenteDeDatos = fuenteDeDatos;
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        return repository.obtenerHechos();
    }

    public List<Hecho> aportarHechos(List<HechoDTO> hechosDTO) throws IOException {
        return repository.aportarHechos(hechosDTO);
    }

    public Hecho cambiarEstado(Hecho hecho) throws IOException {
        return repository.cambiarEstado(hecho);
    }
}
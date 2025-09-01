package utn.dds.fuentes.dinamica.services;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.conexion.Conexion;
import utn.dds.fuentes.dinamica.repositories.HechoRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class ServiceHechoDinamica {
    private final HechoRepository repository;
    private final FuenteDinamicaImpl fuenteDeDatos;
    private final URL url;
    
    public ServiceHechoDinamica(String daoType, Map<String, Object> daoConfig, Conexion conexion) {
        try {
            // Mepa que esto deberia ser el  mock de los hechos que tengo en la carpeta resources
            this.url= URI.create("http://example.com/api/hechos").toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new RuntimeException("Error al crear URL", e);
        }

        this.repository = new HechoRepository(daoType, daoConfig);
        this.fuenteDeDatos = new FuenteDinamicaImpl(conexion, this.url);
    }

    // Aca ver si obtengo los hechos desde el repositorio o desde la fuente
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
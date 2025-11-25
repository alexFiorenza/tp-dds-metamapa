package utn.dds.fuentes.dinamica.services;

import io.javalin.http.UploadedFile;
import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.conexion.Conexion;
import utn.dds.fuentes.dinamica.repositories.HechoRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ServiceHechoDinamica {
    private final HechoRepository repository;
    // private final FuenteDinamicaImpl fuenteDeDatos;  // Tengo dudas de si necesitamos la fuenteDinamicaImpl o no
    // private final URL url;
    
    public ServiceHechoDinamica(String daoType, Map<String, Object> daoConfig) {

        // Esto es para crear la URL de donde voy a obtener los hechos con FuenteDinamicaImpl
        /*
        try {
            // Mepa que esto deberia ser el  mock de los hechos que tengo en la carpeta resources
            this.url= URI.create("http://example.com/api/hechos").toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new RuntimeException("Error al crear URL", e);
        }

        this.fuenteDeDatos = new FuenteDinamicaImpl(conexion, this.url);
        */

        this.repository = new HechoRepository(daoType, daoConfig);
    }

    // Aca ver si obtengo los hechos desde el repositorio o desde la fuente
    public List<Hecho> obtenerHechos() throws IOException {
        return this.repository.obtenerHechos();
    }

    public HechoDTO aportarHecho(HechoDTO hecho) throws IOException {
        return repository.aportarHecho(hecho);
    }

    public HechoDTO aportarHechoConArchivos(HechoDTO hecho, List<UploadedFile> archivos) throws IOException {
        return repository.aportarHechoConArchivos(hecho, archivos);
    }

    public Hecho cambiarEstado(Hecho hecho) throws IOException {
        return repository.cambiarEstado(hecho);
    }
    
    public RespuestaPaginadaDTO<Hecho> obtenerHechosPaginados(int pagina, int tamanioPagina) throws IOException {
        // Validaciones
        if (pagina < 0) {
            pagina = 0;
        }
        if (tamanioPagina <= 0) {
            tamanioPagina = 10; // Tamaño por defecto
        }
        if (tamanioPagina > 100) {
            tamanioPagina = 100; // Máximo 100 elementos por página
        }
        
        // Obtener todos los hechos
        List<Hecho> todosLosHechos = repository.obtenerHechos();
        long totalElementos = todosLosHechos.size();
        
        // Calcular índices para la paginación
        int indiceInicio = pagina * tamanioPagina;
        int indiceFin = Math.min(indiceInicio + tamanioPagina, (int) totalElementos);
        
        // Obtener datos de la página actual
        List<Hecho> datosPagina;
        if (indiceInicio >= totalElementos) {
            datosPagina = new ArrayList<>();
        } else {
            datosPagina = todosLosHechos.subList(indiceInicio, indiceFin);
        }
        
        return new RespuestaPaginadaDTO<>(datosPagina, pagina, tamanioPagina, totalElementos);
    }

    /**
     * Busca un hecho por su UUID
     */
    public HechoDTO buscarHechoPorUuid(String uuid) throws IOException {
        return repository.buscarHechoPorUuid(uuid);
    }

    /**
     * Actualiza un hecho existente
     */
    public HechoDTO actualizarHecho(HechoDTO hechoActualizado, List<UploadedFile> archivos) throws IOException {
        if (archivos != null && !archivos.isEmpty()) {
            return repository.actualizarHechoConArchivos(hechoActualizado, archivos);
        } else {
            return repository.actualizarHecho(hechoActualizado);
        }
    }
}
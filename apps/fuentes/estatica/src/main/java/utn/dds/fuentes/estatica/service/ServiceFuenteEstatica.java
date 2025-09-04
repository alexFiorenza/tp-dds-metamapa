package utn.dds.fuentes.estatica.service;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.estatica.persistencia.HechoRepository;
import utn.dds.fuentes.estatica.service.model.FuenteEstaticaImpl;
import utn.dds.fuentes.estatica.service.model.strategies.ProcesadorStrategy;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class ServiceFuenteEstatica {
    private final HechoRepository hechoRepository;
    private final ProcesadorStrategy procesador;
    
    public ServiceFuenteEstatica(String daoType, Map<String, Object> daoConfig, ProcesadorStrategy procesador) {
        this.hechoRepository = new HechoRepository(daoType, daoConfig);
        this.procesador = procesador;
    }
    
    public List<Hecho> obtenerHechos() throws IOException {
        InputStream data = hechoRepository.leerArchivo();
        FuenteDeDatos fuente = new FuenteEstaticaImpl(data, procesador);
        return fuente.obtenerHechos();
    }

    public List<Hecho> obtenerHechos(String path) throws IOException {
        InputStream data = hechoRepository.leerArchivo(path);
        FuenteDeDatos fuente = new FuenteEstaticaImpl(data, procesador);
        return fuente.obtenerHechos();
    }
    
    public RespuestaPaginadaDTO<Hecho> obtenerHechosPaginados(int pagina, int tamanioPagina) throws IOException {
        return obtenerHechosPaginados(pagina, tamanioPagina, null);
    }
    
    public RespuestaPaginadaDTO<Hecho> obtenerHechosPaginados(int pagina, int tamanioPagina, String path) throws IOException {
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
        List<Hecho> todosLosHechos;
        if (path != null && !path.trim().isEmpty()) {
            todosLosHechos = obtenerHechos(path);
        } else {
            todosLosHechos = obtenerHechos();
        }
        
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
}
package utn.dds.fuentes.proxy.metamapa.service;

import java.util.List;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.proxy.metamapa.service.model.FuenteMetaMapaImpl;

public class ServiceFuenteProxyMetaMapa {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFuenteProxyMetaMapa.class);
    private String url;
    private FuenteDeDatos fuente;

    public ServiceFuenteProxyMetaMapa(String url) {
        this.url = url;
        this.fuente = new FuenteMetaMapaImpl(this.url);
    }
    
    public List<Hecho> obtenerHechos(){
        logger.info("Obteniendo hechos de MetaMapa");
        try{
            return this.fuente.obtenerHechos();
        } catch (Exception e) {
            logger.error("Error al obtener hechos de MetaMapa: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener hechos de MetaMapa: " + e.getMessage(), e);
        }
    }
    
    public RespuestaPaginadaDTO<Hecho> obtenerHechosPaginados(int pagina, int tamanioPagina) {
        logger.info("Obteniendo hechos paginados de MetaMapa - página: {}, tamaño: {}", pagina, tamanioPagina);
        
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
        
        try {
            // Obtener todos los hechos de la fuente
            List<Hecho> todosLosHechos = this.fuente.obtenerHechos();
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
            
        } catch (Exception e) {
            logger.error("Error al obtener hechos paginados de MetaMapa: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener hechos paginados de MetaMapa: " + e.getMessage(), e);
        }
    }
}
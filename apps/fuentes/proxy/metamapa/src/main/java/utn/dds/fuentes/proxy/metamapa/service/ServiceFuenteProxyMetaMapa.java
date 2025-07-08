package utn.dds.fuentes.proxy.metamapa.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
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
}
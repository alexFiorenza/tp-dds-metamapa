package utn.dds.fuentes.estatica.service.model;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;
import utn.dds.fuentes.estatica.service.model.strategies.ProcesadorStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FuenteEstaticaImpl implements FuenteDeDatos {
    private static final Logger logger = LoggerFactory.getLogger(FuenteEstaticaImpl.class);
    
    private final InputStream input;
    private final ProcesadorStrategy procesador;

    public FuenteEstaticaImpl(InputStream input, ProcesadorStrategy procesador) {
        this.input = input;
        this.procesador = procesador;
        logger.info("FuenteEstaticaImpl inicializada con procesador: {}", procesador.getClass().getSimpleName());
    }

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<>();
        logger.info("Iniciando lectura de hechos desde InputStream");
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String linea;
            int lineaNum = 0;
            int hechosValidos = 0;
            
            while ((linea = reader.readLine()) != null) {
                lineaNum++;
                
                Hecho hecho = procesador.procesar(linea);
                if (hecho != null) {
                    hechos.add(hecho);
                    hechosValidos++;
                }
            }
            
            logger.info("Lectura completada. Total líneas: {}, Hechos válidos: {}", lineaNum, hechosValidos);
            
        } catch (IOException e) {
            logger.error("Error al leer la fuente estática: {}", e.getMessage(), e);
            throw new RuntimeException("Error al leer la fuente estática", e);
        }
        
        logger.info("Retornando {} hechos", hechos.size());
        return hechos;
    }

    @Override
    public TipoFuente tipo() {
        return TipoFuente.ESTATICA;
    }
} 
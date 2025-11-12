package utn.dds.fuentes.estatica.service.model.strategies;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.Origen;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoHecho;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVStrategy implements ProcesadorStrategy {
    private static final Logger logger = LoggerFactory.getLogger(CSVStrategy.class);
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private final CSVParser csvParser = new CSVParserBuilder()
            .withSeparator(',')
            .withQuoteChar('"')
            .build();

    @Override
    public Hecho procesar(String linea) {
        try {
            String[] campos = csvParser.parseLine(linea);
            
            if (campos.length < 6) {
                logger.warn("Línea CSV con menos de 6 campos: {} (tiene {})", linea, campos.length);
                return null;
            }
            
            String titulo = campos[0].trim();
            String descripcion = campos[1].trim();
            String categoria = campos[2].trim();
            double latitud = Double.parseDouble(campos[3].trim());
            double longitud = Double.parseDouble(campos[4].trim());
            LocalDate fechaDelHecho = LocalDate.parse(campos[5].trim(), dateFormatter);
            
            List<String> etiquetas = new ArrayList<>();
            List<String> multimedia = new ArrayList<>();
            return new Hecho(
                titulo, descripcion, categoria, fechaDelHecho,
                null, null, TipoHecho.TEXTO,
                longitud, latitud, LocalDateTime.now(),
                EstadoHecho.ACTIVO, etiquetas, multimedia
            );
        } catch (Exception e) {
            logger.error("Error al procesar línea CSV: {} - Error: {}", linea, e.getMessage());
            return null;
        }
    }
} 
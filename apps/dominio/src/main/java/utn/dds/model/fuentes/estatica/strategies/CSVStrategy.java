package utn.dds.model.fuentes.estatica.strategies;

import utn.dds.model.EstadoHecho;
import utn.dds.model.Hecho;
import utn.dds.model.Origen;
import utn.dds.model.TipoHecho;

import java.io.FileReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import com.opencsv.CSVReader;

public class CSVStrategy implements ProcesadorStrategy {
    private final Path path;
    private final String separador;
    private final List<String> etiquetas;

    public CSVStrategy(Path path, String separador) {
        this.path = path;
        this.separador = separador;
        // Ver esto: ¿Las etiquetas seran las mismas para todos los hechos?
        this.etiquetas = new ArrayList<>();
    }

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try (CSVReader csvReader = new CSVReader(new FileReader(path.toFile()))){
            String[] campos;
            csvReader.readNext();

            while ((campos = csvReader.readNext()) != null) {
                if (campos.length < 6) continue; // salteamos líneas inválidas

                String titulo = campos[0].trim();
                String descripcion = campos[1].trim();
                String categoria = campos[2].trim();
                double latitud = Double.parseDouble(campos[3].trim());
                double longitud = Double.parseDouble(campos[4].trim());
                LocalDate fechaDelHecho = LocalDate.parse(campos[5].trim(), dateFormatter);

                Hecho hecho = new Hecho(
                        titulo, descripcion,
                        categoria, fechaDelHecho,
                        Origen.DATASET, null,
                        TipoHecho.TEXTO,
                        longitud,latitud,
                        LocalDateTime.now(),
                        EstadoHecho.ACTIVO,
                        etiquetas
                );
                hechos.add(hecho);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error al procesar el CSV", e);
        }

        return hechos;
    }
} 
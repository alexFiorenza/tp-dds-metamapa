package utn.dds.dominio.fuentes.estatica.strategies;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.Origen;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class CSVStrategy implements ProcesadorStrategy {
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @Override
    public Hecho procesar(String linea) {
        try {
            String[] campos = linea.split(",");
            if (campos.length < 6) return null;
            String titulo = campos[0].trim();
            String descripcion = campos[1].trim();
            String categoria = campos[2].trim();
            double latitud = Double.parseDouble(campos[3].trim());
            double longitud = Double.parseDouble(campos[4].trim());
            LocalDate fechaDelHecho = LocalDate.parse(campos[5].trim(), dateFormatter);
            List<String> etiquetas = new ArrayList<>(); // O parsear si hay más campos
            return new Hecho(
                titulo, descripcion, categoria, fechaDelHecho,
                Origen.DATASET, null, TipoHecho.TEXTO,
                longitud, latitud, LocalDateTime.now(),
                EstadoHecho.ACTIVO, etiquetas
            );
        } catch (Exception e) {
            return null;
        }
    }
} 
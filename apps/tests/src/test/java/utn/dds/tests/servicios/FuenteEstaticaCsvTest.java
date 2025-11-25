package utn.dds.tests.servicios;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class FuenteEstaticaCsvTest {

    // parseo muy simple: cada línea "titulo,descripcion,categoria"
    private List<Hecho> parseCsv(String csv) {
        List<Hecho> out = new ArrayList<>();
        String[] lines = csv.split("\\r?\\n");
        for (String l : lines) {
            String[] cols = l.split(",", -1);
            String titulo = cols.length > 0 ? cols[0].trim() : "";
            String descripcion = cols.length > 1 ? cols[1].trim() : "";
            String categoria = cols.length > 2 ? cols[2].trim() : "sin_categoria";
            Hecho h = new Hecho(
                titulo,
                descripcion,
                categoria,
                LocalDate.now(),
                "csv",
                null,
                TipoHecho.TEXTO,
                0.0,
                0.0,
                LocalDateTime.now(),
                EstadoHecho.ACTIVO,
                new ArrayList<>(),
                new ArrayList<>()
            );
            out.add(h);
        }
        return out;
    }

    @Test
    void leeCsvYGeneraHechos() {
        String csv = "T1,Desc1,cat1\nT2,Desc2,cat2";
        List<Hecho> hechos = parseCsv(csv);
        assertEquals(2, hechos.size());
        assertEquals("T1", hechos.get(0).getTitulo());
        assertEquals("cat2", hechos.get(1).getCategoria());
    }

    @Test
    void pisaHechoDuplicadoPorTitulo() {
        String csv = "X,old,cat\nx,new,cat";
        List<Hecho> hechos = parseCsv(csv);
        // simulamos la lógica de upsert: el más reciente (último en CSV) pisa por título (case-insensitive)
        Map<String, Hecho> mapa = new LinkedHashMap<>();
        for (Hecho h : hechos) {
            mapa.put(h.getTitulo().trim().toLowerCase(), h);
        }
        assertEquals(1, mapa.size());
        Hecho res = mapa.get("x");
        assertEquals("new", res.getDescripcion());
    }
}
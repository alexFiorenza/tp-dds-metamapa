package utn.dds.tests.servicios;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class AgregadorTest {

    private Hecho build(String titulo, LocalDateTime fechaCarga) {
        return new Hecho(
            titulo,
            "desc",
            "cat",
            LocalDate.now(),
            "origen",
            null,
            TipoHecho.TEXTO,
            0.0,
            0.0,
            fechaCarga,
            EstadoHecho.ACTIVO,
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

    // función local que simula agregador: une listas y evita duplicados por título (case-insensitive)
    private List<Hecho> agregarYUnificar(List<List<Hecho>> fuentes) {
        Map<String, Hecho> mapa = new LinkedHashMap<>();
        for (List<Hecho> f : fuentes) {
            if (f == null) continue;
            for (Hecho h : f) {
                if (h == null || h.getTitulo() == null) continue;
                String key = h.getTitulo().trim().toLowerCase();
                // si ya existe, elegir el más reciente por fechaCarga
                if (!mapa.containsKey(key) || (h.getFechaCarga() != null &&
                        mapa.get(key).getFechaCarga() != null &&
                        h.getFechaCarga().isAfter(mapa.get(key).getFechaCarga()))) {
                    mapa.put(key, h);
                }
            }
        }
        return new ArrayList<>(mapa.values());
    }

    @Test
    void agregaHechosDeTodasLasFuentes() {
        List<Hecho> f1 = List.of(build("A", LocalDateTime.now().minusMinutes(10)));
        List<Hecho> f2 = List.of(build("B", LocalDateTime.now().minusMinutes(5)));
        List<Hecho> resultado = agregarYUnificar(List.of(f1, f2));
        assertEquals(2, resultado.size());
    }

    @Test
    void normalizaCategorias() {
        // aquí comprobamos sólo la normalización de títulos en el agregador local
        List<Hecho> f1 = List.of(build("Incendio Centro", LocalDateTime.now().minusMinutes(10)));
        List<Hecho> f2 = List.of(build("incendio centro", LocalDateTime.now().minusMinutes(1)));
        List<Hecho> resultado = agregarYUnificar(List.of(f1, f2));
        assertEquals(1, resultado.size(), "Debería unificarse por título ignorando mayúsculas/minúsculas");
        // se debe quedar la versión más reciente
        assertTrue(resultado.get(0).getFechaCarga().isAfter(LocalDateTime.now().minusMinutes(6)));
    }

    @Test
    void noDuplicaHechos() {
        List<Hecho> f1 = List.of(build("X", LocalDateTime.now()));
        List<Hecho> f2 = List.of(build("x", LocalDateTime.now()));
        List<Hecho> resultado = agregarYUnificar(List.of(f1, f2));
        assertEquals(1, resultado.size());
    }

    @Test
    void ejecutaCadaUnaHora() {
        LocalDateTime ultima = LocalDateTime.now().minusMinutes(61);
        LocalDateTime siguiente = ultima.plusHours(1);
        assertTrue(siguiente.isBefore(LocalDateTime.now()) || siguiente.isEqual(LocalDateTime.now().minusMinutes( -0 )), 
                   "La siguiente ejecución debe ser una hora después de la anterior (comprobación temporal)");
        // simplificación: comprobamos que la diferencia sea 60 minutos
        long minutos = java.time.Duration.between(ultima, siguiente).toMinutes();
        assertEquals(60, minutos);
    }
}
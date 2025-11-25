package utn.dds.tests.dominio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.Fuente;
import utn.dds.dominio.consenso.ConsensoMenciones;
import utn.dds.dominio.consenso.ConsensoSimple;
import utn.dds.dominio.consenso.ConsensoAbsoluto;
import utn.dds.dominio.consenso.AlgoritmoConsenso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConsensoTest {

    private Hecho crearHecho(String titulo, String descripcion, String categoria, String origen) {
        return new Hecho(
            titulo,
            descripcion,
            categoria,
            LocalDate.now(),
            origen,
            null,
            null,
            0.0,
            0.0,
            LocalDateTime.now(),
            null,
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

    private Fuente crearFuente(String host) {
        Fuente fuente = new Fuente();
        fuente.setHost(host);
        return fuente;
    }

    @Test
    void consensoPorMultiplesMencionesAprueba() {
        // Crear algoritmo de consenso
        AlgoritmoConsenso algoritmo = new ConsensoMenciones();

        // Crear fuentes
        List<Fuente> fuentes = Arrays.asList(
            crearFuente("fuente1"),
            crearFuente("fuente2"),
            crearFuente("fuente3")
        );

        // Crear hechos con el mismo título de dos fuentes diferentes
        List<Hecho> hechos = Arrays.asList(
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente1"),
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente2")
        );

        // Filtrar hechos consensuados
        List<Hecho> hechosConsensuados = algoritmo.filtrarHechosConsensuados(hechos, fuentes);

        // Verificar que los hechos están consensuados (2 fuentes mencionan el mismo hecho)
        assertEquals(2, hechosConsensuados.size(), "Deberían haber 2 hechos consensuados");
    }

    @Test
    void consensoPorMultiplesMencionesRechaza() {
        // Crear algoritmo de consenso
        AlgoritmoConsenso algoritmo = new ConsensoMenciones();

        // Crear fuentes
        List<Fuente> fuentes = Arrays.asList(
            crearFuente("fuente1"),
            crearFuente("fuente2"),
            crearFuente("fuente3")
        );

        // Crear hechos con el mismo título pero con conflictos en los datos
        List<Hecho> hechos = Arrays.asList(
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente1"),
            crearHecho("Incendio en el centro", "Descripción completamente diferente", "Otra categoría", "fuente2")
        );

        // Filtrar hechos consensuados
        List<Hecho> hechosConsensuados = algoritmo.filtrarHechosConsensuados(hechos, fuentes);

        // Verificar que no hay hechos consensuados debido a conflictos
        assertTrue(hechosConsensuados.isEmpty(), "No debería haber hechos consensuados debido a conflictos");
    }

    @Test
    void consensoPorMayoriaSimpleAprueba() {
        // Crear algoritmo de consenso
        AlgoritmoConsenso algoritmo = new ConsensoSimple();

        // Crear fuentes
        List<Fuente> fuentes = Arrays.asList(
            crearFuente("fuente1"),
            crearFuente("fuente2"),
            crearFuente("fuente3")
        );

        // Crear hechos con el mismo título de dos fuentes diferentes (mayoría simple: 2/3)
        List<Hecho> hechos = Arrays.asList(
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente1"),
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente2")
        );

        // Filtrar hechos consensuados
        List<Hecho> hechosConsensuados = algoritmo.filtrarHechosConsensuados(hechos, fuentes);

        // Verificar que los hechos están consensuados (mayoría simple: 2/3 fuentes)
        assertEquals(2, hechosConsensuados.size(), "Deberían haber 2 hechos consensuados por mayoría simple");
    }

    @Test
    void consensoPorMayoriaSimpleRechaza() {
        // Crear algoritmo de consenso
        AlgoritmoConsenso algoritmo = new ConsensoSimple();

        // Crear fuentes
        List<Fuente> fuentes = Arrays.asList(
            crearFuente("fuente1"),
            crearFuente("fuente2"),
            crearFuente("fuente3"),
            crearFuente("fuente4"),
            crearFuente("fuente5")
        );

        // Crear hechos con el mismo título pero solo de 2 fuentes (no alcanza mayoría simple: 2/5)
        List<Hecho> hechos = Arrays.asList(
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente1"),
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente2")
        );

        // Filtrar hechos consensuados
        List<Hecho> hechosConsensuados = algoritmo.filtrarHechosConsensuados(hechos, fuentes);

        // Verificar que no hay hechos consensuados (no alcanza mayoría simple: 2/5 fuentes)
        assertTrue(hechosConsensuados.isEmpty(), "No debería haber hechos consensuados por falta de mayoría simple");
    }

    @Test
    void consensoPorUnanimidadAprueba() {
        // Crear algoritmo de consenso
        AlgoritmoConsenso algoritmo = new ConsensoAbsoluto();

        // Crear fuentes
        List<Fuente> fuentes = Arrays.asList(
            crearFuente("fuente1"),
            crearFuente("fuente2"),
            crearFuente("fuente3")
        );

        // Crear hechos con el mismo título de todas las fuentes (unanimidad: 3/3)
        List<Hecho> hechos = Arrays.asList(
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente1"),
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente2"),
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente3")
        );

        // Filtrar hechos consensuados
        List<Hecho> hechosConsensuados = algoritmo.filtrarHechosConsensuados(hechos, fuentes);

        // Verificar que los hechos están consensuados (unanimidad: 3/3 fuentes)
        assertEquals(3, hechosConsensuados.size(), "Deberían haber 3 hechos consensuados por unanimidad");
    }

    @Test
    void consensoPorUnanimidadRechaza() {
        // Crear algoritmo de consenso
        AlgoritmoConsenso algoritmo = new ConsensoAbsoluto();

        // Crear fuentes
        List<Fuente> fuentes = Arrays.asList(
            crearFuente("fuente1"),
            crearFuente("fuente2"),
            crearFuente("fuente3")
        );

        // Crear hechos con el mismo título pero solo de 2 fuentes (no alcanza unanimidad: 2/3)
        List<Hecho> hechos = Arrays.asList(
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente1"),
            crearHecho("Incendio en el centro", "Descripción 1", "Incendio", "fuente2")
        );

        // Filtrar hechos consensuados
        List<Hecho> hechosConsensuados = algoritmo.filtrarHechosConsensuados(hechos, fuentes);

        // Verificar que no hay hechos consensuados (no alcanza unanimidad: 2/3 fuentes)
        assertTrue(hechosConsensuados.isEmpty(), "No debería haber hechos consensuados por falta de unanimidad");
    }
}
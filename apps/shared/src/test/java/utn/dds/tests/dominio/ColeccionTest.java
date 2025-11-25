package utn.dds.tests.dominio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Criterio;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.Fuente;
import utn.dds.dominio.criterios.CategoriaStrategy;
import utn.dds.dominio.criterios.FechaAcontecimientoStrategy;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dominio.consenso.ConsensoMenciones;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColeccionTest {

    private Hecho crearHecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento, String origen) {
        return new Hecho(
            titulo,
            descripcion,
            categoria,
            fechaAcontecimiento,
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
    void hechoPerteneceSegunCriterio() {
        // Crear colección
        Coleccion coleccion = new Coleccion();
        coleccion.setTitulo("Colección de prueba");

        // Crear criterio de pertenencia (categoría = "Incendio")
        Criterio criterio = new Criterio();
        criterio.setTipo("categoria");
        criterio.setCategoria("incendio");

        // Agregar criterio a la colección
        coleccion.setCriteriosDePertenencia(List.of(criterio));

        // Crear hechos
        Hecho hechoQuePertenece = crearHecho(
            "Incendio en el centro", 
            "Descripción", 
            "Incendio", 
            LocalDate.now(), 
            "fuente1"
        );

        Hecho hechoQueNoPertenece = crearHecho(
            "Inundación en el sur", 
            "Descripción", 
            "Inundación", 
            LocalDate.now(), 
            "fuente1"
        );

        // Obtener estrategias de filtrado
        List<HechoStrategy> estrategias = coleccion.getCriteriosDePertenenciaAsStrategies();

        // Verificar que el hecho que pertenece cumple con todos los criterios
        boolean cumpleTodos = true;
        for (HechoStrategy estrategia : estrategias) {
            if (!estrategia.cumple(hechoQuePertenece)) {
                cumpleTodos = false;
                break;
            }
        }

        assertTrue(cumpleTodos, "El hecho con categoría 'Incendio' debería cumplir con el criterio");

        // Verificar que el hecho que no pertenece no cumple con todos los criterios
        cumpleTodos = true;
        for (HechoStrategy estrategia : estrategias) {
            if (!estrategia.cumple(hechoQueNoPertenece)) {
                cumpleTodos = false;
                break;
            }
        }

        assertFalse(cumpleTodos, "El hecho con categoría 'Inundación' no debería cumplir con el criterio");
    }

    @Test
    void filtraPorCategoria() {
        // Crear colección
        Coleccion coleccion = new Coleccion();
        coleccion.setTitulo("Colección de prueba");

        // Crear hechos de diferentes categorías
        Hecho hecho1 = crearHecho("Incendio 1", "Desc 1", "Incendio", LocalDate.now(), "fuente1");
        Hecho hecho2 = crearHecho("Incendio 2", "Desc 2", "Incendio", LocalDate.now(), "fuente2");
        Hecho hecho3 = crearHecho("Inundación 1", "Desc 3", "Inundación", LocalDate.now(), "fuente1");
        Hecho hecho4 = crearHecho("Terremoto 1", "Desc 4", "Terremoto", LocalDate.now(), "fuente2");

        // Agregar hechos a la colección
        coleccion.setHechos(Arrays.asList(hecho1, hecho2, hecho3, hecho4));

        // Crear filtro por categoría "Incendio"
        List<HechoStrategy> filtros = List.of(new CategoriaStrategy("incendio"));

        // Buscar hechos que cumplen con el filtro
        List<Hecho> resultados = coleccion.buscarHechos(filtros);

        // Verificar que solo se encontraron los hechos de categoría "Incendio"
        assertEquals(2, resultados.size(), "Deberían encontrarse 2 hechos de categoría 'Incendio'");
        assertTrue(resultados.contains(hecho1), "El resultado debería contener el hecho1");
        assertTrue(resultados.contains(hecho2), "El resultado debería contener el hecho2");
        assertFalse(resultados.contains(hecho3), "El resultado no debería contener el hecho3");
        assertFalse(resultados.contains(hecho4), "El resultado no debería contener el hecho4");
    }

    @Test
    void filtraPorFecha() {
        // Crear colección
        Coleccion coleccion = new Coleccion();
        coleccion.setTitulo("Colección de prueba");

        // Crear hechos con diferentes fechas
        LocalDate fechaAnterior = LocalDate.of(2023, 1, 1);
        LocalDate fechaPosterior = LocalDate.of(2023, 6, 1);

        Hecho hecho1 = crearHecho("Hecho 1", "Desc 1", "Categoría", fechaAnterior, "fuente1");
        Hecho hecho2 = crearHecho("Hecho 2", "Desc 2", "Categoría", fechaPosterior, "fuente2");

        // Agregar hechos a la colección
        coleccion.setHechos(Arrays.asList(hecho1, hecho2));

        // Crear filtro por fecha posterior a 2023-06-01
        LocalDate fechaFiltro = LocalDate.of(2023, 6, 1);
        List<HechoStrategy> filtros = List.of(new FechaAcontecimientoStrategy(fechaFiltro));

        // Buscar hechos que cumplen con el filtro
        List<Hecho> resultados = coleccion.buscarHechos(filtros);

        // Verificar que solo se encontró el hecho con fecha posterior
        assertEquals(1, resultados.size(), "Debería encontrarse 1 hecho con fecha posterior a 2023-06-01");
        assertTrue(resultados.contains(hecho2), "El resultado debería contener el hecho2");
        assertFalse(resultados.contains(hecho1), "El resultado no debería contener el hecho1");
    }

    @Test
    void incorporaHechosDeMultiplesFuentes() {
        // Crear colección
        Coleccion coleccion = new Coleccion();
        coleccion.setTitulo("Colección de prueba");
        coleccion.setAlgoritmoConsenso(new ConsensoMenciones());

        // Crear fuentes
        Fuente fuente1 = crearFuente("fuente1");
        Fuente fuente2 = crearFuente("fuente2");
        Fuente fuente3 = crearFuente("fuente3");

        // Agregar fuentes a la colección
        coleccion.setFuentes(Arrays.asList(fuente1, fuente2, fuente3));

        // Crear hechos de diferentes fuentes
        Hecho hecho1 = crearHecho("Incendio en el centro", "Desc 1", "Incendio", LocalDate.now(), "fuente1");
        Hecho hecho2 = crearHecho("Incendio en el centro", "Desc 1", "Incendio", LocalDate.now(), "fuente2");
        Hecho hecho3 = crearHecho("Inundación en el sur", "Desc 3", "Inundación", LocalDate.now(), "fuente3");

        // Agregar hechos a la colección
        coleccion.setHechos(Arrays.asList(hecho1, hecho2, hecho3));

        // Verificar que la colección tiene hechos de múltiples fuentes
        List<String> origenes = coleccion.getHechos().stream()
            .map(Hecho::getOrigen)
            .distinct()
            .toList();

        assertEquals(3, origenes.size(), "Deberían haber hechos de 3 fuentes diferentes");
        assertTrue(origenes.contains("fuente1"), "Debería haber hechos de fuente1");
        assertTrue(origenes.contains("fuente2"), "Debería haber hechos de fuente2");
        assertTrue(origenes.contains("fuente3"), "Debería haber hechos de fuente3");

        // Verificar que los hechos consensuados (mencionados por al menos 2 fuentes) son filtrados correctamente
        List<Hecho> hechosConsensuados = coleccion.getAlgoritmoConsenso().filtrarHechosConsensuados(
            coleccion.getHechos(), 
            coleccion.getFuentes()
        );

        assertEquals(2, hechosConsensuados.size(), "Deberían haber 2 hechos consensuados (mismo título en 2 fuentes)");
        assertTrue(hechosConsensuados.contains(hecho1), "El hecho1 debería estar consensuado");
        assertTrue(hechosConsensuados.contains(hecho2), "El hecho2 debería estar consensuado");
        assertFalse(hechosConsensuados.contains(hecho3), "El hecho3 no debería estar consensuado");
    }
}

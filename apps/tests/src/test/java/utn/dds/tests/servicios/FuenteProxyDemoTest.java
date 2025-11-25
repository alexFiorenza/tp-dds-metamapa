package utn.dds.tests.servicios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoHecho;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class FuenteProxyDemoTest {

    // Mock de FuenteDeDatos para simular el comportamiento de FuenteDemoImpl
    private static class MockFuenteProxy implements FuenteDeDatos {
        private final List<Hecho> hechos;
        private final boolean returnNull;
        private final LocalDateTime fechaLimite;

        public MockFuenteProxy(List<Hecho> hechos, boolean returnNull, LocalDateTime fechaLimite) {
            this.hechos = hechos;
            this.returnNull = returnNull;
            this.fechaLimite = fechaLimite;
        }

        @Override
        public List<Hecho> obtenerHechos() {
            if (returnNull) {
                return null;
            }

            if (fechaLimite != null) {
                // Filtrar hechos por fecha
                List<Hecho> hechosFiltrados = new ArrayList<>();
                for (Hecho hecho : hechos) {
                    if (hecho.getFechaCarga().isAfter(fechaLimite)) {
                        hechosFiltrados.add(hecho);
                    }
                }
                return hechosFiltrados;
            }

            return hechos;
        }

        @Override
        public TipoFuente tipo() {
            return TipoFuente.PROXY;
        }
    }

    private Hecho crearHechoPrueba(String titulo, LocalDateTime fechaCarga) {
        return new Hecho(
            titulo,
            "Descripción de prueba",
            "Categoría de prueba",
            LocalDate.now(),
            "Biblioteca externa",
            null, // contribuyente
            TipoHecho.TEXTO,
            -34.603722, // longitud
            -58.381592, // latitud
            fechaCarga,
            EstadoHecho.ACTIVO,
            new ArrayList<>(), // etiquetas
            new ArrayList<>()  // multimedia
        );
    }

    @Test
    void obtieneHechoDesdeBibliotecaExterna() {
        // Crear un hecho de prueba
        Hecho hechoPrueba = crearHechoPrueba("Hecho de biblioteca externa", LocalDateTime.now());
        List<Hecho> hechosPrueba = new ArrayList<>();
        hechosPrueba.add(hechoPrueba);

        // Crear el mock de FuenteDeDatos
        FuenteDeDatos fuenteMock = new MockFuenteProxy(hechosPrueba, false, null);

        // Obtener hechos desde la fuente
        List<Hecho> hechosObtenidos = fuenteMock.obtenerHechos();

        // Verificar que se obtuvo el hecho correctamente
        assertNotNull(hechosObtenidos, "La lista de hechos no debería ser nula");
        assertEquals(1, hechosObtenidos.size(), "Debería haber un hecho en la lista");

        Hecho hechoObtenido = hechosObtenidos.get(0);
        assertEquals("Hecho de biblioteca externa", hechoObtenido.getTitulo(), "El título del hecho debería coincidir");
        assertEquals("Biblioteca externa", hechoObtenido.getOrigen(), "El origen del hecho debería ser 'Biblioteca externa'");
        assertEquals(TipoHecho.TEXTO, hechoObtenido.getTipo(), "El tipo del hecho debería ser TEXTO");
    }

    @Test
    void ignoraNullCuandoNoHayNuevosHechos() {
        // Crear el mock de FuenteDeDatos que devuelve null
        FuenteDeDatos fuenteMock = new MockFuenteProxy(new ArrayList<>(), true, null);

        // Obtener hechos desde la fuente
        List<Hecho> hechosObtenidos = fuenteMock.obtenerHechos();

        // Verificar que se devuelve null cuando no hay nuevos hechos
        assertNull(hechosObtenidos, "La lista de hechos debería ser nula cuando no hay nuevos hechos");
    }

    @Test
    void noTraeHechosConAntiguedadMayorAUnaHora() {
        // Crear hechos con diferentes fechas
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime haceDosHoras = ahora.minusHours(2);
        LocalDateTime haceMediaHora = ahora.minusMinutes(30);

        Hecho hechoAntiguo = crearHechoPrueba("Hecho antiguo", haceDosHoras);
        Hecho hechoReciente = crearHechoPrueba("Hecho reciente", haceMediaHora);

        List<Hecho> hechosPrueba = new ArrayList<>();
        hechosPrueba.add(hechoAntiguo);
        hechosPrueba.add(hechoReciente);

        // Crear el mock de FuenteDeDatos con límite de una hora
        LocalDateTime fechaLimite = ahora.minusHours(1);
        FuenteDeDatos fuenteMock = new MockFuenteProxy(hechosPrueba, false, fechaLimite);

        // Obtener hechos desde la fuente
        List<Hecho> hechosObtenidos = fuenteMock.obtenerHechos();

        // Verificar que solo se obtienen los hechos con antigüedad menor a una hora
        assertNotNull(hechosObtenidos, "La lista de hechos no debería ser nula");
        assertEquals(1, hechosObtenidos.size(), "Debería haber un solo hecho en la lista (el reciente)");

        Hecho hechoObtenido = hechosObtenidos.get(0);
        assertEquals("Hecho reciente", hechoObtenido.getTitulo(), "El título del hecho debería ser 'Hecho reciente'");
        assertTrue(hechoObtenido.getFechaCarga().isAfter(fechaLimite), 
                  "La fecha de carga del hecho debería ser posterior al límite de una hora");
    }
}

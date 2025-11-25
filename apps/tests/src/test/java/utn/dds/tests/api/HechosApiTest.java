package utn.dds.tests.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utn.dds.dominio.Contribuyente;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoSolicitud;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class HechosApiTest {

    private List<Hecho> hechos;
    private List<Hecho> hechosCurados;
    private Map<String, Object> parametros;

    @BeforeEach
    void setUp() {
        // Crear hechos de prueba
        hechos = new ArrayList<>();

        // Hechos activos
        hechos.add(crearHecho("Buenos Aires", "Clima", LocalDateTime.now(), EstadoHecho.ACTIVO));
        hechos.add(crearHecho("Córdoba", "Tráfico", LocalDateTime.now(), EstadoHecho.ACTIVO));

        // Hechos ocultos
        hechos.add(crearHecho("Mendoza", "Clima", LocalDateTime.now(), EstadoHecho.OCULTO));

        // Hechos curados (filtrados por algún criterio)
        hechosCurados = Arrays.asList(hechos.get(0), hechos.get(1));

        // Parámetros de filtrado
        parametros = new HashMap<>();
        parametros.put("categoria", "Clima");
        parametros.put("origen", "Buenos Aires");
    }

    private Hecho crearHecho(String origen, String categoria, LocalDateTime fechaCarga, EstadoHecho estado) {
        Hecho hecho = new Hecho(
            "Título de prueba",
            "Descripción de prueba",
            categoria,
            LocalDate.now(),
            origen,
            new Contribuyente("Contribuyente", "de prueba", 30),
            TipoHecho.TEXTO,
            0.0,
            0.0,
            fechaCarga,
            estado,
            new ArrayList<>(),
            new ArrayList<>()
        );
        return hecho;
    }

    @Test
    void obtieneHechosIrrestrictos() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que obtenemos todos los hechos activos
        List<Hecho> hechosActivos = hechos.stream()
            .filter(h -> h.getEstado() == EstadoHecho.ACTIVO)
            .toList();

        assertEquals(2, hechosActivos.size());
        assertTrue(hechosActivos.contains(hechos.get(0)));
        assertTrue(hechosActivos.contains(hechos.get(1)));
    }

    @Test
    void obtieneHechosCurados() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que obtenemos los hechos curados
        assertEquals(2, hechosCurados.size());
        assertTrue(hechosCurados.contains(hechos.get(0)));
        assertTrue(hechosCurados.contains(hechos.get(1)));
    }

    @Test
    void filtraPorParametros() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que filtramos los hechos por los parámetros
        List<Hecho> hechosFiltrados = hechos.stream()
            .filter(h -> h.getEstado() == EstadoHecho.ACTIVO)
            .filter(h -> h.getCategoria().equals(parametros.get("categoria")))
            .filter(h -> h.getOrigen().equals(parametros.get("origen")))
            .toList();

        assertEquals(1, hechosFiltrados.size());
        assertEquals("Clima", hechosFiltrados.get(0).getCategoria());
        assertEquals("Buenos Aires", hechosFiltrados.get(0).getOrigen());
    }

    @Test
    void creaSolicitudDeEliminacion() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que creamos una solicitud de eliminación
        String hechoId = hechos.get(0).getUuid();
        String texto = "Este hecho contiene información incorrecta";

        SolicitudEliminacion solicitud = new SolicitudEliminacion(
            texto,
            hechoId,
            LocalDateTime.now(),
            EstadoSolicitud.ACTIVO,
            UUID.randomUUID().toString()
        );

        assertNotNull(solicitud);
        assertEquals(hechoId, solicitud.getHecho());
        assertEquals(texto, solicitud.getTexto());
        assertEquals(EstadoSolicitud.ACTIVO, solicitud.getEstado());
    }

    @Test
    void reportaHecho() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que reportamos un hecho (lo ocultamos)
        Hecho hecho = hechos.get(0);
        hecho.ocultar();

        assertEquals(EstadoHecho.OCULTO, hecho.getEstado());
    }
}

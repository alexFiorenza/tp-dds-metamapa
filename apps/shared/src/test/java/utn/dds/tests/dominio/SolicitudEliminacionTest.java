package utn.dds.tests.dominio;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.EstadoSolicitud;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

public class SolicitudEliminacionTest {

    private Hecho crearHecho() {
        return new Hecho(
            "Título de prueba",
            "Descripción de prueba",
            "Categoría de prueba",
            LocalDate.now(),
            "Origen de prueba",
            null,
            null,
            0.0,
            0.0,
            LocalDateTime.now(),
            EstadoHecho.ACTIVO,
            new ArrayList<>(),
            new ArrayList<>()
        );
    }

    @Test
    void creaSolicitudConTextoSuficiente() {
        // Crear un hecho
        Hecho hecho = crearHecho();

        // Crear una solicitud de eliminación con texto suficiente (más de 10 caracteres)
        String textoSuficiente = "Este es un texto suficientemente largo para justificar la eliminación del hecho.";
        SolicitudEliminacion solicitud = new SolicitudEliminacion(
            textoSuficiente,
            hecho.getUuid(),
            LocalDateTime.now(),
            EstadoSolicitud.ACTIVO,
            UUID.randomUUID().toString()
        );

        // Verificar que la solicitud se creó correctamente
        assertNotNull(solicitud.getUuid(), "El UUID de la solicitud no debería ser nulo");
        assertEquals(textoSuficiente, solicitud.getTexto(), "El texto de la solicitud debería ser el mismo que se proporcionó");
        assertEquals(hecho.getUuid(), solicitud.getHecho(), "El UUID del hecho debería ser el mismo que se proporcionó");
        assertEquals(EstadoSolicitud.ACTIVO, solicitud.getEstado(), "El estado inicial de la solicitud debería ser ACTIVO");
    }

    @Test
    void aceptarSolicitudOcultaHecho() {
        // Crear un hecho
        Hecho hecho = crearHecho();

        // Verificar que el hecho está activo inicialmente
        assertEquals(EstadoHecho.ACTIVO, hecho.getEstado(), "El hecho debería estar activo inicialmente");

        // Crear una solicitud de eliminación
        SolicitudEliminacion solicitud = new SolicitudEliminacion(
            "Texto de justificación para eliminar el hecho",
            hecho.getUuid(),
            LocalDateTime.now(),
            EstadoSolicitud.ACTIVO,
            UUID.randomUUID().toString()
        );

        // Aceptar la solicitud (ocultar el hecho)
        hecho.ocultar();

        // Verificar que el hecho está oculto
        assertEquals(EstadoHecho.OCULTO, hecho.getEstado(), "El hecho debería estar oculto después de aceptar la solicitud");
    }

    @Test
    void rechazarSolicitudEliminaSolicitud() {
        // Crear un hecho
        Hecho hecho = crearHecho();

        // Crear una solicitud de eliminación
        SolicitudEliminacion solicitud = new SolicitudEliminacion(
            "Texto de justificación para eliminar el hecho",
            hecho.getUuid(),
            LocalDateTime.now(),
            EstadoSolicitud.ACTIVO,
            UUID.randomUUID().toString()
        );

        // Verificar que la solicitud está activa inicialmente
        assertEquals(EstadoSolicitud.ACTIVO, solicitud.getEstado(), "La solicitud debería estar activa inicialmente");

        // Rechazar la solicitud (ocultar la solicitud)
        solicitud.ocultar();

        // Verificar que la solicitud está oculta
        assertEquals(EstadoSolicitud.OCULTO, solicitud.getEstado(), "La solicitud debería estar oculta después de rechazarla");

        // Verificar que el hecho sigue activo
        assertEquals(EstadoHecho.ACTIVO, hecho.getEstado(), "El hecho debería seguir activo después de rechazar la solicitud");
    }
}
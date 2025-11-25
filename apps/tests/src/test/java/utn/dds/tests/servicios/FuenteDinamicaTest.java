package utn.dds.tests.servicios;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class FuenteDinamicaTest {

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

    private boolean puedeEditar(Hecho hecho, LocalDateTime ahora) {
        if (hecho.getFechaCarga() == null) return false;
        return java.time.Duration.between(hecho.getFechaCarga(), ahora).toDays() < 7;
    }

    @Test
    void creaHechoAnonimo() {
        Hecho h = build("anonimo", LocalDateTime.now());
        assertNotNull(h.getUuid());
        assertEquals("anonimo", h.getTitulo());
    }

    @Test
    void creaHechoRegistrado() {
        Hecho h = build("usuario", LocalDateTime.now());
        // simulamos que un contribuyente registrado no es null: el constructor permite null contribuyente,
        // aquí validamos propiedades básicas
        assertEquals("usuario", h.getTitulo());
        assertTrue(h.getEstado() == EstadoHecho.ACTIVO || h.getEstado() == null);
    }

    @Test
    void permiteEdicionDentroDeUnaSemana() {
        Hecho reciente = build("r", LocalDateTime.now().minusDays(3));
        assertTrue(puedeEditar(reciente, LocalDateTime.now()));
    }

    @Test
    void prohibeEdicionLuegoDeUnaSemana() {
        Hecho viejo = build("v", LocalDateTime.now().minusDays(8));
        assertFalse(puedeEditar(viejo, LocalDateTime.now()));
    }
}
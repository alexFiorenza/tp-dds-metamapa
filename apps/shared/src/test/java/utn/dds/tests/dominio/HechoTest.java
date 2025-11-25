package utn.dds.tests.dominio;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public class HechoTest {

    private Hecho buildHecho(String titulo,
                             String descripcion,
                             String categoria,
                             LocalDate fechaAcontecimiento,
                             String origen,
                             List<String> etiquetas,
                             List<String> multimedia) {
        // Contribuyente y TipoHecho no son necesarios para estos tests; pasamos null.
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
                etiquetas,
                multimedia
        );
    }

    @Test
    void creaHechoConDatosCorrectos() {
        LocalDate fecha = LocalDate.of(2025, 10, 30);
        List<String> etiquetas = List.of("tag1", "tag2");
        List<String> multimedia = List.of("http://example.com/1.jpg");

        Hecho h = buildHecho("Titulo A", "Descripcion A", "Categoria A", fecha, "Origen A", etiquetas, multimedia);

        assertNotNull(h.getUuid(), "UUID debe existir");
        assertEquals("Titulo A", h.getTitulo());
        assertEquals("Descripcion A", h.getDescripcion());
        assertEquals("Categoria A", h.getCategoria());
        assertEquals(fecha, h.getFechaAcontecimiento());
        assertEquals("Origen A", h.getOrigen());
        assertEquals(etiquetas, h.getEtiquetas());
        assertEquals(multimedia, h.getMultimedia());
    }



    @Test
    void actualizaHechoAlImportarCsvPisandoAtributos() {
        // existente
        Hecho existente = buildHecho("Titulo X", "Desc old", "Cat old", LocalDate.of(2025,1,1), "Orig old",
                List.of("old"), List.of("oldUrl"));
        // nuevo (simula fila CSV que debe pisar atributos)
        Hecho nuevo = buildHecho("Titulo X", "Desc new", "Cat new", LocalDate.of(2025,12,31), "Orig new",
                List.of("n1","n2"), List.of("newUrl1","newUrl2"));

        // lógica de merge: sobrescribir campos con valores del nuevo
        existente.setUuid(nuevo.getUuid()); // mantener mismo UUID
        existente.setDescripcion(nuevo.getDescripcion());
        existente.setCategoria(nuevo.getCategoria());
        existente.setFechaAcontecimiento(nuevo.getFechaAcontecimiento());
        existente.setOrigen(nuevo.getOrigen());
        existente.setEtiquetas(nuevo.getEtiquetas());
        existente.setMultimedia(nuevo.getMultimedia());

        assertEquals("Desc new", existente.getDescripcion());
        assertEquals("Cat new", existente.getCategoria());
        assertEquals(LocalDate.of(2025,12,31), existente.getFechaAcontecimiento());
        assertEquals("Orig new", existente.getOrigen());
        assertEquals(List.of("n1","n2"), existente.getEtiquetas());
        assertEquals(List.of("newUrl1","newUrl2"), existente.getMultimedia());
    }

}
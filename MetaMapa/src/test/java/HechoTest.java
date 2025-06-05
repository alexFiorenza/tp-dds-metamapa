import org.junit.jupiter.api.Test;
import utn.dds.model.Hecho;
import utn.dds.model.fuentes.estatica.strategies.CSVStrategy;
import utn.dds.service.negocio.FuenteDeDatos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HechoTest {

    /**
     * Como persona administradora,
     * deseo poder importar hechos desde un archivo CSV.
     */
    @Test
    void importarHechosDesdeUnCSV(){
        Path path = Paths.get("src/test/resources/data/desastres_sanitarios_contaminacion_argentina.csv");
        String separador = ",";
        FuenteDeDatos fuente = new FuenteDeDatos();

        List<Hecho> hechosImportados = fuente.importarDesdeEstatica(new CSVStrategy(path,separador));
        assertNotNull(hechosImportados, "La lista de hechos no debería ser nula");
        assertFalse(hechosImportados.isEmpty(), "La lista de hechos debería tener elementos");

        Hecho primerHecho = hechosImportados.get(0);
        assertInstanceOf(Hecho.class, primerHecho, "El primer elemento debe ser una instancia de Hecho");
        assertNotNull(primerHecho.getTitulo(), "El título no debería ser nulo");
        assertNotNull(primerHecho.getDescripcion(), "La descripción no debería ser nula");
        assertNotNull(primerHecho.getCategoria(), "La categoría no debería ser nula");
        assertNotNull(primerHecho.getFechaAcontecimiento(), "La fecha no debería ser nula");
        assertTrue(primerHecho.getLatitud() >= -90 && primerHecho.getLatitud() <= 90, "Latitud válida");
        assertTrue(primerHecho.getLongitud() >= -180 && primerHecho.getLongitud() <= 180, "Longitud válida");
    }
}

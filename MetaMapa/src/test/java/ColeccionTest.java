import org.junit.jupiter.api.Test;
import utn.dds.model.Hecho;
import utn.dds.model.HechoStrategy;
import utn.dds.model.fuentes.estatica.strategies.CSVStrategy;
import utn.dds.service.negocio.ColeccionService;
import utn.dds.service.negocio.FuenteDeDatos;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ColeccionTest {
    /**
     * Como persona administradora,
     * deseo crear una colección.
     */
    @Test
    void crearUnaColeccion() {
        List<Hecho> hechos = new ArrayList<>();
        List<HechoStrategy> criteriosDePertenencia = new ArrayList<>();
        String titulo ="Desastres sanitarios";
        String descripcion = "Desastres sanitarios en Argentina";
        ColeccionService coleccionService = new ColeccionService(
                titulo, descripcion, hechos, criteriosDePertenencia
        );
        assertNotNull(coleccionService);
    }


    /**
     * Como persona visualizadora,
     * deseo navegar todos los hechos disponibles de una colección.
     */
    @Test
    void navegarHechos(){
        /*
         * Instancio Servicio Coleccion
         */

        //TODO: Agregar criterios de Pertenencia (podriamos usar factory como patron junto a strategy)
        List<HechoStrategy> criteriosDePertenencia = new ArrayList<>();

        String titulo ="Desastres sanitarios";
        String descripcion = "Desastres sanitarios en Argentina";
        ColeccionService coleccionService = new ColeccionService(
                titulo, descripcion, new ArrayList<>(), criteriosDePertenencia
        );

        /*
         * Instancio Servicio Fuente De Datos
         */
        Path path = Paths.get("src/test/resources/data/desastres_sanitarios_contaminacion_argentina.csv");
        String separador = ",";
        FuenteDeDatos fuente = new FuenteDeDatos();


        /*
         * Carga de hechos
         */
        coleccionService.cargarHechos(fuente.importarDesdeEstatica(new CSVStrategy(path,separador)));

        /*
         * Navegar (Verífico que no sean nulos por ejemplo)
         */
        List<Hecho> hechos = coleccionService.hechos();
        assertFalse(hechos.isEmpty(), "La colección no debería estar vacía");
        for (Hecho hecho : hechos) {
            assertNotNull(hecho, "El hecho no debe ser null");
            assertNotNull(hecho.getTitulo(), "El título del hecho no debe ser null");
            assertNotNull(hecho.getDescripcion(), "La descripción no debe ser null");
            assertNotNull(hecho.getCategoria(), "La categoría no debe ser null");
            assertNotNull(hecho.getFechaAcontecimiento(), "La fecha del hecho no debe ser null");
        }
    }

    /**
     * Como persona visualizadora,
     * deseo navegar los hechos disponibles de una colección, aplicando
     * filtros.
     */
    @Test
    void navegarHechosConFiltros(){
    }
}

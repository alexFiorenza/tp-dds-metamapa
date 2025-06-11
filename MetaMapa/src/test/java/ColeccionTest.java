import org.junit.jupiter.api.Test;
import utn.dds.model.Hecho;
import utn.dds.model.criterios.CategoriaStrategy;
import utn.dds.model.criterios.HechoStrategy;
import utn.dds.model.criterios.TituloStrategy;
import utn.dds.model.fuentes.estatica.strategies.CSVStrategy;
import utn.dds.service.negocio.ColeccionService;
import utn.dds.service.negocio.FuenteDeDatosService;

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
        FuenteDeDatosService fuente = new FuenteDeDatosService();


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
        String titulo ="Desastres sanitarios";
        String descripcion = "Desastres sanitarios en Argentina";
        ColeccionService coleccionService = new ColeccionService(
                titulo, descripcion, new ArrayList<>(), new ArrayList<>()
        );

        /*
         * Instancio Servicio Fuente De Datos
         */
        Path path = Paths.get("src/test/resources/data/desastres_sanitarios_contaminacion_argentina.csv");
        String separador = ",";
        FuenteDeDatosService fuente = new FuenteDeDatosService();


        /*
         * Carga de hechos
         */
        coleccionService.cargarHechos(fuente.importarDesdeEstatica(new CSVStrategy(path,separador)));

        /*
         * Navego aplicando un filtro por Titulo y descripcion
         */
        List<HechoStrategy> filtros = new ArrayList<>();
        filtros.add(new TituloStrategy("jujuy"));
        filtros.add(new CategoriaStrategy("desastre"));
        List<Hecho> hechos = coleccionService.buscarHechos(filtros);
        assertFalse(hechos.isEmpty(), "La colección no debería estar vacía");
    }
}

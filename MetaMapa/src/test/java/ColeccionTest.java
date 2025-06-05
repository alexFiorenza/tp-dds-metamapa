import org.junit.jupiter.api.Test;
import utn.dds.model.Hecho;
import utn.dds.model.HechoStrategy;
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

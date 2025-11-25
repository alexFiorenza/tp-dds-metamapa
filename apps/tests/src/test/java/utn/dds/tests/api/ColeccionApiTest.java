package utn.dds.tests.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Criterio;
import utn.dds.dominio.Fuente;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.consenso.AlgoritmoConsenso;
import utn.dds.dominio.consenso.ConsensoAbsoluto;
import utn.dds.dominio.consenso.ConsensoDefault;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ColeccionApiTest {

    private Coleccion coleccion;
    private List<Hecho> hechos;
    private List<Criterio> criterios;
    private List<Fuente> fuentes;

    @BeforeEach
    void setUp() {
        hechos = new ArrayList<>();
        criterios = new ArrayList<>();
        fuentes = new ArrayList<>();

        // Crear fuentes de prueba
        Fuente fuente1 = new Fuente();

        fuentes.add(fuente1);

        Fuente fuente2 = new Fuente();

        fuentes.add(fuente2);
    }

    @Test
    void creaColeccion() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que creamos una colección
        coleccion = new Coleccion("Colección de prueba", "Descripción de prueba", hechos, criterios);

        assertNotNull(coleccion);
        assertEquals("Colección de prueba", coleccion.getTitulo());
        assertEquals("Descripción de prueba", coleccion.getDescripcion());
        assertTrue(coleccion.getHechos().isEmpty());
        assertTrue(coleccion.getCriteriosDePertenencia().isEmpty());
    }

    @Test
    void modificaAlgoritmoDeConsenso() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que modificamos el algoritmo de consenso
        coleccion = new Coleccion("Colección de prueba", "Descripción de prueba", hechos, criterios);

        // Por defecto, el algoritmo es ConsensoDefault
        assertInstanceOf(ConsensoDefault.class, coleccion.getAlgoritmoConsenso());

        // Modificamos el algoritmo a ConsensoAbsoluto
        AlgoritmoConsenso nuevoAlgoritmo = new ConsensoAbsoluto();
        coleccion.setAlgoritmoConsenso(nuevoAlgoritmo);

        assertInstanceOf(ConsensoAbsoluto.class, coleccion.getAlgoritmoConsenso());
    }

    @Test
    void agregaFuenteALaColeccion() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que agregamos una fuente a la colección
        coleccion = new Coleccion("Colección de prueba", "Descripción de prueba", hechos, criterios);

        // Inicialmente no hay fuentes
        assertTrue(coleccion.getFuentes().isEmpty());

        // Agregamos una fuente
        coleccion.setFuentes(List.of(fuentes.get(0)));

        assertEquals(1, coleccion.getFuentes().size());

    }

    @Test
    void quitaFuenteDeLaColeccion() {
        // En un entorno real, esto sería una llamada a un servicio o controlador
        // Aquí simulamos que quitamos una fuente de la colección
        coleccion = new Coleccion("Colección de prueba", "Descripción de prueba", hechos, criterios);

        // Agregamos dos fuentes
        coleccion.setFuentes(fuentes);
        assertEquals(2, coleccion.getFuentes().size());

        // Quitamos una fuente
        List<Fuente> nuevasFuentes = new ArrayList<>(coleccion.getFuentes());
        nuevasFuentes.remove(0);
        coleccion.setFuentes(nuevasFuentes);

        assertEquals(1, coleccion.getFuentes().size());

    }
}

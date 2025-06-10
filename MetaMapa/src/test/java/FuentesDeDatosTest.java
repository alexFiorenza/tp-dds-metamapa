import org.junit.jupiter.api.Test;
import utn.dds.model.Hecho;
import utn.dds.repository.HechoRepository;
import utn.dds.service.negocio.ContribuyenteService;
import utn.dds.model.Contribuyente;
import utn.dds.model.fuentes.estatica.strategies.CSVStrategy;
import utn.dds.service.negocio.FuenteDeDatosService;
import utn.dds.model.Origen;
import utn.dds.model.TipoHecho;
import utn.dds.model.EstadoHecho;



import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FuentesDeDatosTest {
    /**
     * Como persona administradora,
     * deseo poder importar hechos desde un archivo CSV.
     */
    @Test
    void importarHechosDesdeUnCSV(){
        Path path = Paths.get("src/test/resources/data/desastres_sanitarios_contaminacion_argentina.csv");
        String separador = ",";

        FuenteDeDatosService fuente = new FuenteDeDatosService();
        List<Hecho> hechosImportados = fuente.importarDesdeArchivo(new CSVStrategy(path,separador));
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


    /**
     * Como persona contribuyente,
     * deseo poder crear un hecho a partir de una fuente dinamica.
     */
    void crearDesdeFuenteDinamica(){

        // Desarrollo del caso
         ContribuyenteService contribuyenteService  = new ContribuyenteService();
         Contribuyente contribuyente = new Contribuyente("Pedro", "Ruiz", 22);
         LocalDate fechaCarga = LocalDate.now(); // para el test hardcodeado, hay que borrarlo
         List<String> etiquetas = List.of("urgente", "transporte"); //para la prueba

         Origen origen;
         TipoHecho tipoHecho;
         EstadoHecho estadoHecho;


         Hecho hecho = contribuyenteService.aportarHecho(contribuyente, "Ataque en el bosque", "3 muertos",
                 "Natural", fechaCarga, Origen.CONTRIBUYENTE, TipoHecho.TEXTO, 0, 0, fechaCarga,
                 EstadoHecho.OCULTO, etiquetas); // devuelve un hecho

         // dentro de aportarHecho() seria:
                // HechoRepository hechoRepository = new HechoRepository();
                // Hecho hecho = hechoRepository.nuevoHechoDinamico(datos del hecho, datos del contribuyente) -> devuelve un hecho
                // return hecho;

         // assertNotNull(hecho,'el hecho creado dinamicamente no tendria que ser nulo');
         // otros tests ...
    }
}

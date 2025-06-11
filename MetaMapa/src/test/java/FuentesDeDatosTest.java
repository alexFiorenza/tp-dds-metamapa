import org.junit.jupiter.api.Test;
import utn.dds.model.Hecho;
import utn.dds.model.fuentes.proxy.Conexion;
import utn.dds.service.negocio.ContribuyenteService;
import utn.dds.model.Contribuyente;
import utn.dds.model.fuentes.estatica.strategies.CSVStrategy;
import utn.dds.service.negocio.FuenteDeDatosService;
import utn.dds.model.Origen;
import utn.dds.model.TipoHecho;
import utn.dds.model.EstadoHecho;


import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Map.entry;
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


    /**
     * Como persona contribuyente,
     * deseo poder crear un hecho a partir de una fuente dinamica.
     */
    @Test
    void crearDesdeFuenteDinamica(){

        // Desarrollo del caso
        Contribuyente contribuyente = new Contribuyente("Pedro", "Ruiz", 22);
        ContribuyenteService contribuyenteService  = new ContribuyenteService(contribuyente);
        LocalDateTime fechaCarga = LocalDateTime.now(); // para el test hardcodeado, hay que borrarlo
        List<String> etiquetas = List.of("urgente", "transporte"); //para la prueba

        Hecho hecho = contribuyenteService.aportarHecho("Ataque en el bosque", "3 muertos",
                "Natural", LocalDate.now(),TipoHecho.TEXTO, 0, 0, fechaCarga,
                 EstadoHecho.OCULTO, etiquetas); // devuelve un hecho


        assertNotNull(hecho,"el hecho creado dinamicamente no tendria que ser nulo");
        assertNotNull(hecho.getTitulo(), "El título no debería ser nulo");
        assertNotNull(hecho.getDescripcion(), "La descripción no debería ser nula");
        assertNotNull(hecho.getCategoria(), "La categoría no debería ser nula");
        assertTrue(hecho.getLatitud() >= -90 && hecho.getLatitud() <= 90, "Latitud válida");
        assertTrue(hecho.getLongitud() >= -180 && hecho.getLongitud() <= 180, "Longitud válida");

    }

    /**
     * Como persona usuaria, quiero poder obtener todos los hechos de una fuente proxy demo configurada
     * en una colección, con un nivel de antigüedad máximo de una hora.
     */
    @Test
    void importarDesdeFuenteDemo(){
        FuenteDeDatosService fuente = new FuenteDeDatosService();
        try {
            URL url = new URL("https://demo/api/hechos");
            List<Hecho> hechos = fuente.importarDesdeDemo(new ConexionMock(),url);
            assertNotNull(hechos,"Tengo una lista de hechos");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}

class ConexionMock implements Conexion {
    @Override
    public Map<String, Object> obtenerDatos(URL url, LocalDateTime fechaUltimaConsulta) {
        if (fechaUltimaConsulta == null) {
            // Mockeo la conexion para que cuando me ejecutan por primera vez (con fecha nula) devuelva algo y la proxima vez devuelva null
            Map<String, Object> datos = new HashMap<>();
            datos.put("titulo", "Derrame tóxico en Río Matanza");
            datos.put("descripcion", "Se detectó un derrame de químicos en las aguas del río.");
            datos.put("categoria", "Contaminación");
            datos.put("fecha_contecimiento", LocalDate.of(2023, 6, 12));
            datos.put("contribuyente", null);  // Aceptado acá
            datos.put("tipo", TipoHecho.TEXTO);
            datos.put("longitud", -58.4216);
            datos.put("latitud", -34.6037);
            datos.put("fecha_carga", LocalDateTime.now());
            datos.put("estado", EstadoHecho.ACTIVO);
            datos.put("etiquetas", List.of("agua", "químicos", "ambiental"));
            datos.put("uuid", "hecho-0001");

            return datos;
        }else{
            return null;
        }
    }
}





utn.dds.tests.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.metamapa.persistencia.HechoRepository;
import utn.dds.metamapa.persistencia.ColeccionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class ColeccionRepositoryTest {/*

    private HechoRepository hechoRepository;
    private ColeccionRepository coleccionRepository;
    private Map<String, Object> testDbConfig;

    @BeforeEach
    void setUp() {
        // Configuración para usar una base de datos en memoria para las pruebas
        testDbConfig = new HashMap<>();
        testDbConfig.put("jakarta.persistence.jdbc.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        testDbConfig.put("jakarta.persistence.jdbc.user", "sa");
        testDbConfig.put("jakarta.persistence.jdbc.password", "");
        testDbConfig.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        testDbConfig.put("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
        // Create schema but don't drop it at the end
        testDbConfig.put("hibernate.hbm2ddl.auto", "create");
        testDbConfig.put("hibernate.show_sql", "true");
        testDbConfig.put("hibernate.format_sql", "true");
        testDbConfig.put("hibernate.connection.autocommit", "true");
        // Override the default schema to avoid issues with the "public" schema
        testDbConfig.put("hibernate.default_schema", "");
        // Disable foreign key constraint creation
        testDbConfig.put("hibernate.temp.use_jdbc_metadata_defaults", "false");
        // Disable schema validation
        testDbConfig.put("hibernate.validator.apply_to_ddl", "false");
        // Create tables in the correct order
        testDbConfig.put("hibernate.hbm2ddl.import_files_sql_extractor", "org.hibernate.tool.hbm2ddl.MultipleLinesSqlCommandExtractor");
        // Use a different database for each test
        testDbConfig.put("hibernate.connection.url", "jdbc:h2:mem:test_coleccion;DB_CLOSE_DELAY=-1");

        // Inicializar los repositorios con la configuración de prueba
        hechoRepository = new HechoRepository(testDbConfig);
        coleccionRepository = new ColeccionRepository(testDbConfig);
    }

    @AfterEach
    void tearDown() {
        // Cerrar los repositorios para liberar recursos
        if (hechoRepository != null) {
            hechoRepository.close();
        }
        if (coleccionRepository != null) {
            coleccionRepository.close();
        }
    }

    @Test
    void persisteColeccionConSusHechos() {
        // Crear varios Hechos para la prueba
        List<Hecho> hechos = new ArrayList<>();

        // Hecho 1
        Hecho hecho1 = new Hecho(
            "Título del hecho 1",
            "Descripción del hecho 1",
            "Categoría 1",
            LocalDate.now(),
            "Origen 1",
            null, // contribuyente
            TipoHecho.TEXTO,
            -34.603722, // longitud
            -58.381592, // latitud
            LocalDateTime.now(),
            EstadoHecho.ACTIVO,
            new ArrayList<>(), // etiquetas
            new ArrayList<>()  // multimedia
        );

        // Hecho 2
        Hecho hecho2 = new Hecho(
            "Título del hecho 2",
            "Descripción del hecho 2",
            "Categoría 2",
            LocalDate.now(),
            "Origen 2",
            null, // contribuyente
            TipoHecho.MULTIMEDIA,
            -34.603722, // longitud
            -58.381592, // latitud
            LocalDateTime.now(),
            EstadoHecho.ACTIVO,
            new ArrayList<>(), // etiquetas
            new ArrayList<>()  // multimedia
        );

        // Guardar los Hechos en la base de datos
        hechoRepository.guardar(hecho1);
        hechoRepository.guardar(hecho2);

        // Verificar que los Hechos se guardaron correctamente
        assertNotNull(hecho1.getUuid(), "El UUID del Hecho 1 no debería ser nulo después de guardarlo");
        assertNotNull(hecho2.getUuid(), "El UUID del Hecho 2 no debería ser nulo después de guardarlo");

        // Agregar los Hechos a la lista
        hechos.add(hecho1);
        hechos.add(hecho2);

        // Crear una Colección que referencia a los Hechos
        Coleccion coleccion = new Coleccion(
            "Título de la colección",
            "Descripción de la colección",
            hechos,
            new ArrayList<>() // criterios de pertenencia
        );

        // Ensure fuentes list is initialized to avoid issues with foreign key constraints
        coleccion.setFuentes(new ArrayList<>());

        // Guardar la Colección en la base de datos
        coleccionRepository.crear(coleccion);

        // Verificar que la Colección se guardó correctamente
        assertNotNull(coleccion.getHandle(), "El handle de la Colección no debería ser nulo después de guardarla");

        // Recuperar la Colección de la base de datos
        Coleccion coleccionRecuperada = coleccionRepository.obtenerPorId(coleccion.getHandle());

        // Verificar que la Colección recuperada coincide con la original
        assertNotNull(coleccionRecuperada, "La Colección recuperada no debería ser nula");
        assertEquals(coleccion.getHandle(), coleccionRecuperada.getHandle(), "El handle de la Colección recuperada debería coincidir");
        assertEquals("Título de la colección", coleccionRecuperada.getTitulo(), "El título de la Colección recuperada debería coincidir");
        assertEquals("Descripción de la colección", coleccionRecuperada.getDescripcion(), "La descripción de la Colección recuperada debería coincidir");

        // Verificar que la relación con los Hechos se mantuvo
        assertNotNull(coleccionRecuperada.getHechos(), "La lista de Hechos de la Colección recuperada no debería ser nula");
        assertEquals(2, coleccionRecuperada.getHechos().size(), "La Colección recuperada debería tener 2 Hechos");

        // Verificar que los Hechos recuperados coinciden con los originales
        List<String> hechosUuids = coleccionRecuperada.getHechos().stream()
            .map(Hecho::getUuid)
            .toList();

        assertTrue(hechosUuids.contains(hecho1.getUuid()), "La Colección recuperada debería contener el Hecho 1");
        assertTrue(hechosUuids.contains(hecho2.getUuid()), "La Colección recuperada debería contener el Hecho 2");
    }
}*/
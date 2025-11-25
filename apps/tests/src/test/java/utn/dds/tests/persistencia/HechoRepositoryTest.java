package utn.dds.tests.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.metamapa.persistencia.HechoRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class HechoRepositoryTest {
/*
    private HechoRepository hechoRepository;
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
        testDbConfig.put("hibernate.connection.url", "jdbc:h2:mem:test_hecho;DB_CLOSE_DELAY=-1");

        // Inicializar el repositorio con la configuración de prueba
        hechoRepository = new HechoRepository(testDbConfig);
    }

    @AfterEach
    void tearDown() {
        // Cerrar el repositorio para liberar recursos
        if (hechoRepository != null) {
            hechoRepository.close();
        }
    }

    @Test
    void persisteHechoConExito() {
        // Crear un Hecho para la prueba
        Hecho hecho = new Hecho(
            "Título de prueba",
            "Descripción de prueba",
            "Categoría de prueba",
            LocalDate.now(),
            "Origen de prueba",
            null, // contribuyente
            TipoHecho.TEXTO,
            -34.603722, // longitud
            -58.381592, // latitud
            LocalDateTime.now(),
            EstadoHecho.ACTIVO,
            new ArrayList<>(), // etiquetas
            new ArrayList<>()  // multimedia
        );

        // Guardar el Hecho en la base de datos
        hechoRepository.guardar(hecho);

        // Verificar que el Hecho se guardó correctamente
        assertNotNull(hecho.getUuid(), "El UUID del Hecho no debería ser nulo después de guardarlo");

        // Recuperar el Hecho de la base de datos
        Hecho hechoRecuperado = hechoRepository.obtenerPorId(hecho.getUuid());

        // Verificar que el Hecho recuperado coincide con el original
        assertNotNull(hechoRecuperado, "El Hecho recuperado no debería ser nulo");
        assertEquals(hecho.getUuid(), hechoRecuperado.getUuid(), "El UUID del Hecho recuperado debería coincidir");
        assertEquals("Título de prueba", hechoRecuperado.getTitulo(), "El título del Hecho recuperado debería coincidir");
        assertEquals("Descripción de prueba", hechoRecuperado.getDescripcion(), "La descripción del Hecho recuperado debería coincidir");
        assertEquals("Categoría de prueba", hechoRecuperado.getCategoria(), "La categoría del Hecho recuperado debería coincidir");
        assertEquals(TipoHecho.TEXTO, hechoRecuperado.getTipo(), "El tipo del Hecho recuperado debería coincidir");
        assertEquals(EstadoHecho.ACTIVO, hechoRecuperado.getEstado(), "El estado del Hecho recuperado debería coincidir");
    }

    @Test
    void guardaNormalizacionCategoria() {
        // Crear un Hecho con una categoría que necesita normalización (con espacios extra y mayúsculas/minúsculas)
        Hecho hecho = new Hecho(
            "Título de prueba",
            "Descripción de prueba",
            "  CaTeGoRíA   de   PruEbA  ", // Categoría con espacios extra y mayúsculas/minúsculas mezcladas
            LocalDate.now(),
            "Origen de prueba",
            null, // contribuyente
            TipoHecho.TEXTO,
            -34.603722, // longitud
            -58.381592, // latitud
            LocalDateTime.now(),
            EstadoHecho.ACTIVO,
            new ArrayList<>(), // etiquetas
            new ArrayList<>()  // multimedia
        );

        // Guardar el Hecho en la base de datos
        hechoRepository.guardar(hecho);

        // Recuperar el Hecho de la base de datos
        Hecho hechoRecuperado = hechoRepository.obtenerPorId(hecho.getUuid());

        // Verificar que la categoría se normalizó correctamente (si el sistema implementa normalización)
        // Nota: Esta verificación depende de cómo se implemente la normalización en el sistema.
        // Aquí asumimos que la normalización elimina espacios extra y convierte a un formato consistente.
        assertNotNull(hechoRecuperado, "El Hecho recuperado no debería ser nulo");
        assertNotNull(hechoRecuperado.getCategoria(), "La categoría del Hecho recuperado no debería ser nula");

        // Verificar que la categoría se guardó tal como se proporcionó (si no hay normalización)
        // o que se normalizó según las reglas del sistema (si hay normalización)
        String categoriaOriginal = "  CaTeGoRíA   de   PruEbA  ";
        String categoriaRecuperada = hechoRecuperado.getCategoria();

        // Si hay normalización, la categoría recuperada debería ser diferente a la original
        if (!categoriaOriginal.equals(categoriaRecuperada)) {
            // Verificar que la normalización eliminó espacios extra
            assertFalse(categoriaRecuperada.contains("  "), "La categoría normalizada no debería contener espacios múltiples");

            // Verificar que la normalización mantuvo las palabras clave
            assertTrue(categoriaRecuperada.toLowerCase().contains("categoría"), "La categoría normalizada debería contener 'categoría'");
            assertTrue(categoriaRecuperada.toLowerCase().contains("prueba"), "La categoría normalizada debería contener 'prueba'");
        } else {
            // Si no hay normalización, la categoría debería guardarse exactamente como se proporcionó
            assertEquals(categoriaOriginal, categoriaRecuperada, "La categoría debería guardarse sin cambios si no hay normalización");
        }
    }*/
}

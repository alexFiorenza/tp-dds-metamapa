package utn.dds.tests.persistencia;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoSolicitud;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.metamapa.persistencia.HechoRepository;
import utn.dds.metamapa.persistencia.SolicitudEliminacionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

public class SolicitudRepositoryTest {/*

    private HechoRepository hechoRepository;
    private SolicitudEliminacionRepository solicitudRepository;
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
        testDbConfig.put("hibernate.connection.url", "jdbc:h2:mem:test_solicitud;DB_CLOSE_DELAY=-1");

        // Inicializar los repositorios con la configuración de prueba
        hechoRepository = new HechoRepository(testDbConfig);
        solicitudRepository = new SolicitudEliminacionRepository(testDbConfig);
    }

    @AfterEach
    void tearDown() {
        // Cerrar los repositorios para liberar recursos
        if (hechoRepository != null) {
            hechoRepository.close();
        }
        if (solicitudRepository != null) {
            solicitudRepository.close();
        }
    }

    @Test
    void persisteSolicitudYRelacionConHecho() {
        // Crear un Hecho para la prueba
        Hecho hecho = new Hecho(
            "Título de prueba",
            "Descripción de prueba",
            "Categoría de prueba",
            LocalDate.now(),
            "Origen de prueba",
            null, // contribuyente
            TipoHecho.TEXTO, // Usando TEXTO en lugar de EVENTO
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

        // Crear una SolicitudEliminacion que referencia al Hecho
        String solicitudUuid = UUID.randomUUID().toString();
        SolicitudEliminacion solicitud = new SolicitudEliminacion(
            "Texto de la solicitud de eliminación",
            hecho.getUuid(), // Referencia al UUID del Hecho
            LocalDateTime.now(),
            EstadoSolicitud.ACTIVO,
            solicitudUuid
        );

        // Guardar la SolicitudEliminacion en la base de datos
        solicitudRepository.crear(solicitud);

        // Recuperar la SolicitudEliminacion de la base de datos
        SolicitudEliminacion solicitudRecuperada = solicitudRepository.obtenerPorId(solicitudUuid);

        // Verificar que la SolicitudEliminacion se guardó correctamente
        assertNotNull(solicitudRecuperada, "La solicitud recuperada no debería ser nula");
        assertEquals(solicitudUuid, solicitudRecuperada.getUuid(), "El UUID de la solicitud recuperada debería coincidir");
        assertEquals(hecho.getUuid(), solicitudRecuperada.getHecho(), "La referencia al Hecho debería mantenerse");
        assertEquals("Texto de la solicitud de eliminación", solicitudRecuperada.getTexto(), "El texto de la solicitud debería coincidir");
        assertEquals(EstadoSolicitud.ACTIVO, solicitudRecuperada.getEstado(), "El estado de la solicitud debería ser ACTIVO");
    }*/
}

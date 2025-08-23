package utn.dds.metamapa;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.metamapa.config.AppConfig;
import utn.dds.metamapa.controller.ApiRoutes;
import utn.dds.metamapa.controller.AdministradorRoutes;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            AppConfig appConfig = AppConfig.fromEnvironment();
            
            logger.info("Iniciando servicio MetaMapa con configuración:");
            logger.info("  - DAO Type: {}", appConfig.getDaoType());

            // Inicializar routers
            ApiRoutes apiRoutes = new ApiRoutes(appConfig);
            AdministradorRoutes administradorRoutes = new AdministradorRoutes(appConfig);

            Javalin app = Javalin.create(config -> {
                config.plugins.enableDevLogging();
                config.jsonMapper(new io.javalin.json.JavalinJackson().updateMapper(mapper -> {
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }));
            });

            // Rutas base del servicio
            app.get("/health", ctx -> ctx.result("OK"));
            app.get("/", ctx -> ctx.result("MetaMapa - Servicio principal"));

            // Configurar rutas de API y Administrador
            apiRoutes.configure(app);
            administradorRoutes.configure(app);

            app.start(7006);
            
            logger.info("Servicio MetaMapa iniciado en puerto 7006");
            
        } catch (Exception e) {
            logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
} 
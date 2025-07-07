package utn.dds.fuentes.proxy.demo;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.fuentes.proxy.demo.config.AppConfig;
import utn.dds.fuentes.proxy.demo.controller.ControllerFuenteProxyDemo;
import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            AppConfig appConfig = AppConfig.fromEnvironment();
            
            logger.info("Iniciando servicio proxy demo con configuración:");
            logger.info("  - DAO Type: {}", appConfig.getDaoType());
            logger.info("  - Tiempo Cache: {} segundos", appConfig.getTiempoCache());
            
            // Verificar si se debe usar persistencia
            String daoType = appConfig.getDaoType();
            IDAO<String> dao = DAOFactory.createDAO(daoType, appConfig.getDaoConfig());

            ControllerFuenteProxyDemo controller = new ControllerFuenteProxyDemo(dao);
            

            Javalin app = Javalin.create(config -> {
                config.plugins.enableDevLogging();
                config.jsonMapper(new io.javalin.json.JavalinJackson().updateMapper(mapper -> {
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }));
            });
            
            app.get("/health", ctx -> ctx.result("OK"));
            app.get("/", ctx -> ctx.result("Proxy Demo - MetaMapa"));
            app.get("/hechos", controller::obtenerHechos);
            app.put("/hechos", controller::actualizarCache);

            app.start(7004);
            
            logger.info("Servicio proxy demo iniciado en puerto 7004");
            
        } catch (Exception e) {
            logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
} 
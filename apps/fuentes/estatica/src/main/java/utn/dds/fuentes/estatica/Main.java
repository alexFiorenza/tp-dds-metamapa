package utn.dds.fuentes.estatica;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.fuentes.estatica.config.AppConfig;
import utn.dds.fuentes.estatica.controller.ControllerFuenteEstatica;
import utn.dds.fuentes.estatica.service.model.strategies.ProcesadorStrategy;
import utn.dds.fuentes.estatica.service.model.strategies.ProcessorFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            // Leer configuración desde variables de entorno
            AppConfig appConfig = AppConfig.fromEnvironment();
            
            logger.info("Iniciando servicio con configuración:");
            logger.info("  - DAO Type: {}", appConfig.getDaoType());
            logger.info("  - Processor Type: {}", appConfig.getProcessorType());
            logger.info("  - Data URL: {}", appConfig.getDataUrl());
            
            // Logs adicionales para S3
            if (appConfig.getDaoType().equalsIgnoreCase("s3")) {
                logger.info("  - S3 Bucket: {}", appConfig.getDaoConfig().get("bucket"));
                logger.info("  - S3 Endpoint: {}", appConfig.getDaoConfig().get("endpoint"));
                logger.info("  - S3 Region: {}", appConfig.getDaoConfig().get("region"));
                logger.info("  - S3 Access Key: {}***", 
                    ((String) appConfig.getDaoConfig().get("accessKey")).substring(0, 3));
            }
            
            // Crear instancias 
            ProcesadorStrategy procesador = ProcessorFactory.createProcessor(appConfig.getProcessorType());
            
            // Crear controller con las dependencias necesarias
            ControllerFuenteEstatica controller = new ControllerFuenteEstatica(appConfig.getDaoType(), appConfig.getDaoConfig(), procesador);
            
            Javalin app = Javalin.create(config -> {
                config.plugins.enableDevLogging();
                config.jsonMapper(new io.javalin.json.JavalinJackson().updateMapper(mapper -> {
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }));
                
            });
            
            app.get("/health", ctx -> ctx.result("OK"));
            app.get("/", ctx -> ctx.result("Fuentes Estáticas - MetaMapa"));
            app.get("/hechos", controller::obtenerHechos);
            
            app.start(7001);
            
            logger.info("Servicio de fuentes estáticas iniciado en puerto 7001");
            
        } catch (Exception e) {
            logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
} 
package utn.dds.fuentes.dinamica;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.daos.IDAO;
import utn.dds.fuentes.dinamica.controllers.ControllerHechoDinamica;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.config.AppConfig;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        AppConfig appConfig = AppConfig.fromEnvironment();

        logger.info("Iniciando servicio fuente dinamica con configuración:");
        logger.info("  - DAO Type: {}", appConfig.getDaoType());
        logger.info("  - Processor Type: {}", appConfig.getProcessorType());
        logger.info("  - API Endpoint: {}", appConfig.getApiEndpoint());

        ControllerHechoDinamica controller = new ControllerHechoDinamica(appConfig.getDaoType(), new FuenteDinamicaImpl()); // falta implementar esto

        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7002);
        
        // Health check
        app.get("/health", ctx -> ctx.result("OK"));
        
        // Endpoint principal
        app.get("/", ctx -> {
            ctx.result("Fuentes Dinámicas - MetaMapa");
        });
        
        // Endpoint para obtener datos dinámicos
        app.get("/datos", ctx -> {
            ctx.json(new RespuestaDatos("Datos dinámicos disponibles"));
        });

        // Pedimos los hechos
        app.get("/hechos-dinamica", controller::obtenerHechos);

        // Agregamos un hecho (falta pasarle el hecho a agregar y no se como)
        app.post("/hechos-dinamica", controller::agregarHecho);
        
        logger.info("Servicio de fuentes dinámicas iniciado en puerto 7002");
    }
    
    public static class RespuestaDatos {
        public String mensaje;
        
        public RespuestaDatos(String mensaje) {
            this.mensaje = mensaje;
        }
    }
} 
package utn.dds.metamapa.controller;

import io.javalin.Javalin;
import utn.dds.metamapa.config.AppConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiRoutes {
    private static final Logger logger = LoggerFactory.getLogger(ApiRoutes.class);
    private final AppConfig appConfig;

    public ApiRoutes(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public void configure(Javalin app) {
        logger.info("Configurando rutas de API...");
        
        // Rutas base de la API
        app.get("/api/", ctx -> ctx.result("MetaMapa API - Endpoints públicos"));
        app.get("/api/health", ctx -> ctx.result("API OK"));
        
        // Aca se pueden agregar más endpoints de API pública
        // Por ejemplo:
        // app.get("/api/colecciones", controller::obtenerColeccionesPublicas);
        // app.get("/api/hechos", controller::obtenerHechosPublicos);
        
        logger.info("Rutas de API configuradas correctamente");
    }
}
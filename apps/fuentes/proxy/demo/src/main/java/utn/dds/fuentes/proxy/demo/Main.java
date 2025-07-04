package utn.dds.fuentes.proxy.demo;

import io.javalin.Javalin;
import utn.dds.dominio.Hecho;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {
        int TIEMPO_CACHE = Integer.parseInt(
            System.getenv().getOrDefault("tiempo_cache_segundos", "1800")
        );

        logger.info("Tiempo de cache: {} segundos", TIEMPO_CACHE);

        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7004);
        
        // Health check
        app.get("/health", ctx -> ctx.result("OK"));
        
        // Endpoint principal
        app.get("/", ctx -> {
            ctx.result("Proxy Demo - MetaMapa");
        });
        
        // Endpoint para obtener datos del proxy demo
        app.get("/hechos", ctx -> {
           ctx.result("Datos del proxy demo disponibles");
        });
        
        logger.info("Servicio proxy demo iniciado en puerto 7004");
    }
} 
package utn.dds.fuentes.dinamica;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
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
        
        logger.info("Servicio de fuentes dinámicas iniciado en puerto 7002");
    }
    
    public static class RespuestaDatos {
        public String mensaje;
        
        public RespuestaDatos(String mensaje) {
            this.mensaje = mensaje;
        }
    }
} 
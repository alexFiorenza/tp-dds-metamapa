package utn.dds.fuentes.proxy.demo;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
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
        app.get("/datos", ctx -> {
            ctx.json(new RespuestaDatos("Datos del proxy demo disponibles"));
        });
        
        logger.info("Servicio proxy demo iniciado en puerto 7004");
    }
    
    public static class RespuestaDatos {
        public String mensaje;
        
        public RespuestaDatos(String mensaje) {
            this.mensaje = mensaje;
        }
    }
} 
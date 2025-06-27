package utn.dds.fuentes.proxy.metamapa;

import io.javalin.Javalin;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7003);
        
        // Health check
        app.get("/health", ctx -> ctx.result("OK"));
        
        // Endpoint principal
        app.get("/", ctx -> {
            ctx.result("Proxy MetaMapa - MetaMapa");
        });
        
        // Endpoint para obtener datos del proxy
        app.get("/datos", ctx -> {
            ctx.json(new RespuestaDatos("Datos del proxy MetaMapa disponibles"));
        });
        
        logger.info("Servicio proxy MetaMapa iniciado en puerto 7003");
    }
    
    public static class RespuestaDatos {
        public String mensaje;
        
        public RespuestaDatos(String mensaje) {
            this.mensaje = mensaje;
        }
    }
} 
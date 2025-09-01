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
        app.get("/hechos-dinamica", ctx -> {
            ctx.result(new controller::obtenerHechos());
        });

        // Agregamos hechos
        app.post("/hechos-dinamica", ctx -> {
            ctx.result(new controller::agregarHechos());
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
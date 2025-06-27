package utn.dds.agregador;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7005);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/", ctx -> ctx.result("Agregador - MetaMapa"));

        logger.info("Servicio Agregador iniciado en puerto 7005");
    }
} 
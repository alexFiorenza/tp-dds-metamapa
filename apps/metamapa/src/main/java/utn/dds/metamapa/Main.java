package utn.dds.metamapa;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7006);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/", ctx -> ctx.result("MetaMapa - Servicio principal"));

        logger.info("Servicio MetaMapa iniciado en puerto 7006");
    }
} 
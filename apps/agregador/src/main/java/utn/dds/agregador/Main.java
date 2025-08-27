package utn.dds.agregador;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.agregador.controller.RegistryController;
import utn.dds.agregador.service.ServiceRegistry;
import utn.dds.agregador.persistencia.FuentesRepository;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        FuentesRepository fuentesRepository = new FuentesRepository();
        ServiceRegistry serviceRegistry = new ServiceRegistry(fuentesRepository);
        RegistryController registryController = new RegistryController(serviceRegistry);

        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7005);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/", ctx -> ctx.result("Agregador - MetaMapa"));
        
        app.post("/fuentes", registryController::registrar);
        app.get("/fuentes", registryController::obtenerFuentes);
        app.get("/fuentes/{url}", registryController::obtenerFuentePorUrl);
        app.delete("/fuentes/{url}", registryController::eliminarFuente);

        logger.info("Servicio Agregador iniciado en puerto 7005");
        logger.info("Service Registry endpoints disponibles:");
        logger.info("POST /fuentes - Registrar nueva fuente");
        logger.info("GET /fuentes - Obtener todas las fuentes");
    }
} 
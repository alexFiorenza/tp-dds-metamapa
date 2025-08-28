package utn.dds.agregador;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.agregador.controller.RegistryController;
import utn.dds.agregador.controller.ControllerAgregador;
import utn.dds.agregador.service.ServiceRegistry;
import utn.dds.agregador.service.ServiceAgregador;
import utn.dds.agregador.persistencia.FuentesRepository;
import utn.dds.agregador.persistencia.HechoRepository;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        FuentesRepository fuentesRepository = new FuentesRepository();
        HechoRepository hechoRepository = new HechoRepository();
        ServiceRegistry serviceRegistry = new ServiceRegistry(fuentesRepository);
        ServiceAgregador serviceAgregador = new ServiceAgregador(hechoRepository, serviceRegistry);
        RegistryController registryController = new RegistryController(serviceRegistry);
        ControllerAgregador controllerAgregador = new ControllerAgregador(serviceAgregador);

        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7005);

        app.get("/health", ctx -> ctx.result("OK"));
        app.get("/", ctx -> ctx.result("Agregador - MetaMapa"));
        
        app.post("/fuentes", registryController::registrar);
        app.get("/fuentes", registryController::obtenerFuentes);
        app.get("/fuentes/{url}", registryController::obtenerFuentePorUrl);
        app.delete("/fuentes/{url}", registryController::eliminarFuente);
        
        app.post("/agregacion", controllerAgregador::agregacion);
        app.get("/hechos", controllerAgregador::obtenerHechos);

        logger.info("Servicio Agregador iniciado en puerto 7005");
        logger.info("Service Registry endpoints disponibles:");
        logger.info("POST /fuentes - Registrar nueva fuente");
        logger.info("GET /fuentes - Obtener todas las fuentes");
        logger.info("Agregador endpoints disponibles:");
        logger.info("POST /agregacion - Ejecutar proceso de agregación");
        logger.info("GET /hechos - Obtener hechos agregados");
    }
} 
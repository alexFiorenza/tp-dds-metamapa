package utn.dds.agregador;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import utn.dds.agregador.config.AppConfig;
import utn.dds.agregador.config.MetricsConfig;
import utn.dds.agregador.controller.RegistryController;
import utn.dds.agregador.controller.ControllerAgregador;
import utn.dds.agregador.service.ServiceRegistry;
import utn.dds.agregador.service.ServiceAgregador;
import utn.dds.persistencia.FuentesRepository;
import utn.dds.agregador.persistencia.HechoRepository;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @OpenApi(
        summary = "Health check del servicio agregador",
        operationId = "healthCheck",
        path = "/health",
        methods = HttpMethod.GET,
        tags = {"Health"},
        responses = {
            @OpenApiResponse(status = "200", description = "Servicio funcionando correctamente")
        }
    )
    private static void healthCheck(io.javalin.http.Context ctx) {
        ctx.result("OK");
    }
    
    @OpenApi(
        summary = "Información del servicio agregador",
        operationId = "infoServicio",
        path = "/",
        methods = HttpMethod.GET,
        tags = {"Información"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del servicio")
        }
    )
    private static void infoServicio(io.javalin.http.Context ctx) {
        ctx.result("Agregador - MetaMapa");
    }
    

    public static void main(String[] args) {
        AppConfig appConfig = AppConfig.fromEnvironment();
        MetricsConfig metricsConfig = new MetricsConfig();

        FuentesRepository fuentesRepository = new FuentesRepository(appConfig.getDaoConfig());
        HechoRepository hechoRepository = new HechoRepository(appConfig.getDaoConfig());
        ServiceRegistry serviceRegistry = new ServiceRegistry(fuentesRepository);
        ServiceAgregador serviceAgregador = new ServiceAgregador(hechoRepository, serviceRegistry);
        RegistryController registryController = new RegistryController(serviceRegistry);
        ControllerAgregador controllerAgregador = new ControllerAgregador(serviceAgregador);

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            
            // Configurar OpenAPI Plugin siguiendo el ejemplo funcional
            config.registerPlugin(new OpenApiPlugin(openApiConfig -> {
                openApiConfig
                    .withDocumentationPath("/openapi")
                    .withDefinitionConfiguration((version, openApiDefinition) -> {
                        openApiDefinition
                            .withInfo(openApiInfo -> {
                                openApiInfo
                                    .title("MetaMapa - Servicio Agregador API")
                                    .version("1.0.0")
                                    .description("API para la agregación y gestión de fuentes de datos en MetaMapa")
                                    .contact("Equipo MetaMapa", "", "contacto@metamapa.com");
                            });
                            // No especificamos servidor - Swagger UI usará la URL actual automáticamente
                    });
            }));
            
            // Registrar Swagger Plugin
            config.registerPlugin(new SwaggerPlugin());
            
            // Registrar ReDoc Plugin
            config.registerPlugin(new ReDocPlugin());
            
        }).start(7005);

        app.get("/health", Main::healthCheck);
        app.get("/", Main::infoServicio);
        app.get("/metrics", ctx -> ctx.contentType("text/plain; version=0.0.4").result(metricsConfig.scrape()));

        app.post("/fuentes", registryController::registrar);
        app.get("/fuentes", registryController::obtenerFuentes);
        app.get("/fuentes/{host}", registryController::obtenerFuentePorHost);
        app.delete("/fuentes/{uuid}", registryController::eliminarFuente);

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
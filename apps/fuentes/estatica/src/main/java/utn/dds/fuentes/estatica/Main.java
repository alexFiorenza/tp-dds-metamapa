package utn.dds.fuentes.estatica;

import io.javalin.Javalin;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.fuentes.estatica.config.AppConfig;
import utn.dds.fuentes.estatica.controller.ControllerFuenteEstatica;
import utn.dds.fuentes.estatica.service.model.strategies.ProcesadorStrategy;
import utn.dds.fuentes.estatica.service.model.strategies.ProcessorFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @OpenApi(
        summary = "Health check del servicio fuentes estáticas",
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
        summary = "Información del servicio fuentes estáticas",
        operationId = "infoServicio",
        path = "/",
        methods = HttpMethod.GET,
        tags = {"Información"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del servicio")
        }
    )
    private static void infoServicio(io.javalin.http.Context ctx) {
        ctx.result("Fuentes Estáticas - MetaMapa");
    }
    
    public static Javalin createApp() {
        try {
            AppConfig appConfig = AppConfig.fromEnvironment();

            logger.info("Configurando aplicación:");
            logger.info("  - DAO Type: {}", appConfig.getDaoType());
            logger.info("  - Processor Type: {}", appConfig.getProcessorType());

            if (appConfig.getDaoType().equalsIgnoreCase("s3")) {
                logger.info("  - S3 Bucket: {}", appConfig.getDaoConfig().get("bucket"));
                logger.info("  - S3 Endpoint: {}", appConfig.getDaoConfig().get("endpoint"));
                logger.info("  - S3 Region: {}", appConfig.getDaoConfig().get("region"));
                logger.info("  - S3 Access Key: {}***",
                    ((String) appConfig.getDaoConfig().get("accessKey")).substring(0, 3));
            }

            ProcesadorStrategy procesador = ProcessorFactory.createProcessor(appConfig.getProcessorType());
            ControllerFuenteEstatica controller = new ControllerFuenteEstatica(
                appConfig.getDaoType(),
                appConfig.getDaoConfig(),
                procesador
            );

            Javalin app = Javalin.create(config -> {
                config.bundledPlugins.enableDevLogging();
                config.jsonMapper(new io.javalin.json.JavalinJackson().updateMapper(mapper -> {
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }));

                if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") == null) {
                    config.registerPlugin(new OpenApiPlugin(openApiConfig -> {
                        openApiConfig
                            .withDocumentationPath("/openapi")
                            .withDefinitionConfiguration((version, openApiDefinition) -> {
                                openApiDefinition
                                    .withInfo(openApiInfo -> {
                                        openApiInfo
                                            .title("MetaMapa - Fuentes Estáticas API")
                                            .version("1.0.0")
                                            .description("API para acceso a fuentes de datos estáticas (CSV) en MetaMapa")
                                            .contact("Equipo MetaMapa", "", "contacto@metamapa.com");
                                    });
                            });
                    }));

                    config.registerPlugin(new SwaggerPlugin());
                    config.registerPlugin(new ReDocPlugin());
                }
            });

            configureRoutes(app, controller);
            return app;

        } catch (Exception e) {
            logger.error("Error al crear la aplicación: {}", e.getMessage(), e);
            throw new RuntimeException("Error al crear la aplicación", e);
        }
    }

    private static void configureRoutes(Javalin app, ControllerFuenteEstatica controller) {
        if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") == null) {
            app.get("/health", Main::healthCheck);
            app.get("/", Main::infoServicio);
        }

        app.get("/hechos", controller::obtenerHechos);
    }

    public static void main(String[] args) {
        if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null) {
            return;
        }

        try {
            Javalin app = createApp();
            app.start(7001);
            logger.info("Servicio de fuentes estáticas iniciado en puerto 7001");
        } catch (Exception e) {
            logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
} 
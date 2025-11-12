package utn.dds.fuentes.proxy.demo;

import io.javalin.Javalin;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.fuentes.proxy.demo.config.AppConfig;
import utn.dds.fuentes.proxy.demo.controller.ControllerFuenteProxyDemo;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @OpenApi(
        summary = "Health check del servicio proxy demo",
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
        summary = "Información del servicio proxy demo",
        operationId = "infoServicio",
        path = "/",
        methods = HttpMethod.GET,
        tags = {"Información"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del servicio")
        }
    )
    private static void infoServicio(io.javalin.http.Context ctx) {
        ctx.result("Proxy Demo - MetaMapa");
    }
    
    public static Javalin createApp() {
        try {
            AppConfig appConfig = AppConfig.fromEnvironment();

            logger.info("Configurando aplicación:");
            logger.info("  - DAO Type: {}", appConfig.getDaoType());

            ControllerFuenteProxyDemo controller = new ControllerFuenteProxyDemo(appConfig.getDaoType(), appConfig.getDaoConfig());

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
                                            .title("MetaMapa - Proxy Demo API")
                                            .version("1.0.0")
                                            .description("API proxy demo para pruebas y desarrollo en MetaMapa")
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

    private static void configureRoutes(Javalin app, ControllerFuenteProxyDemo controller) {
        if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") == null) {
            app.get("/health", Main::healthCheck);
            app.get("/", Main::infoServicio);
        }

        app.get("/hechos", controller::obtenerHechos);
        app.put("/hechos", controller::agregarHechos);
    }

    public static void main(String[] args) {
        if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null) {
            return;
        }

        try {
            Javalin app = createApp();
            app.start(7004);
            logger.info("Servicio proxy demo iniciado en puerto 7004");
        } catch (Exception e) {
            logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
} 
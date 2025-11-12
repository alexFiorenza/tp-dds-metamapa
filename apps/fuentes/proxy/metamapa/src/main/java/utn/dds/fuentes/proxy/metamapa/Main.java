package utn.dds.fuentes.proxy.metamapa;

import io.javalin.Javalin;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import utn.dds.fuentes.proxy.metamapa.controller.ControllerProxyMetamapa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @OpenApi(
        summary = "Health check del servicio proxy MetaMapa",
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
        summary = "Información del servicio proxy MetaMapa",
        operationId = "infoServicio",
        path = "/",
        methods = HttpMethod.GET,
        tags = {"Información"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del servicio")
        }
    )
    private static void infoServicio(io.javalin.http.Context ctx) {
        ctx.result("Proxy MetaMapa - MetaMapa");
    }
    
    public static Javalin createApp() {
        String url = System.getenv("url");
        if (url == null) {
            url = "https://56d05c91-ca57-4d58-acd5-6d61368e622a.mock.pstmn.io";
        }

        logger.info("Configurando aplicación:");
        logger.info("Metamapa URL: {}", url);

        ControllerProxyMetamapa controller = new ControllerProxyMetamapa(url);

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
                                        .title("MetaMapa - Proxy MetaMapa API")
                                        .version("1.0.0")
                                        .description("API proxy para comunicación con el servicio principal de MetaMapa")
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
    }

    private static void configureRoutes(Javalin app, ControllerProxyMetamapa controller) {
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

        Javalin app = createApp();
        app.start(7003);
        logger.info("Servicio proxy MetaMapa iniciado en puerto 7003");
    }
} 
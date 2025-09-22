package utn.dds.metamapa;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import utn.dds.metamapa.config.AppConfig;
import utn.dds.metamapa.controller.ApiRoutes;
import utn.dds.metamapa.controller.AdministradorRoutes;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    @OpenApi(
        summary = "Health check del servicio MetaMapa",
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
        summary = "Información del servicio MetaMapa",
        operationId = "infoServicio",
        path = "/",
        methods = HttpMethod.GET,
        tags = {"Información"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del servicio principal")
        }
    )
    private static void infoServicio(io.javalin.http.Context ctx) {
        ctx.result("MetaMapa - Servicio principal");
    }

    public static void main(String[] args) {
        try {
            AppConfig appConfig = AppConfig.fromEnvironment();
            
            logger.info("Iniciando servicio MetaMapa con configuración:");
            logger.info("  - DAO Type: {}", appConfig.getDaoType());

            // Inicializar routers
            ApiRoutes apiRoutes = new ApiRoutes(appConfig);
            AdministradorRoutes administradorRoutes = new AdministradorRoutes(appConfig);

            Javalin app = Javalin.create(config -> {
                config.bundledPlugins.enableDevLogging();
                config.jsonMapper(new io.javalin.json.JavalinJackson().updateMapper(mapper -> {
                    mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                    mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }));

                // Configurar OpenAPI Plugin
                config.registerPlugin(new OpenApiPlugin(openApiConfig -> {
                    openApiConfig
                        .withDocumentationPath("/openapi")
                        .withDefinitionConfiguration((version, openApiDefinition) -> {
                            openApiDefinition
                                .withInfo(openApiInfo -> {
                                    openApiInfo
                                        .title("MetaMapa - Servicio Principal API")
                                        .version("1.0.0")
                                        .description("API del servicio principal MetaMapa para administración y consultas")
                                        .contact("Equipo MetaMapa", "http://localhost:7006", "contacto@metamapa.com");
                                })
                                .withServer(openApiServer -> {
                                    openApiServer
                                        .url("http://localhost:7006")
                                        .description("Servidor de desarrollo");
                                });
                        });
                }));

                // Registrar Swagger Plugin
                config.registerPlugin(new SwaggerPlugin());

                // Registrar ReDoc Plugin
                config.registerPlugin(new ReDocPlugin());
            });

            // Rutas base del servicio
            app.get("/health", Main::healthCheck);
            app.get("/", Main::infoServicio);

            // Configurar rutas de API y Administrador
            apiRoutes.configure(app);
            administradorRoutes.configure(app);

            app.start(7006);
            
            logger.info("Servicio MetaMapa iniciado en puerto 7006");
            
        } catch (Exception e) {
            logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
} 
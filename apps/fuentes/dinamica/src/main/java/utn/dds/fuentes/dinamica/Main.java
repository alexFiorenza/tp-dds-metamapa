package utn.dds.fuentes.dinamica;

import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.openapi.*;
import io.javalin.openapi.plugin.OpenApiPlugin;
import io.javalin.openapi.plugin.redoc.ReDocPlugin;
import io.javalin.openapi.plugin.swagger.SwaggerPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.daos.IDAO;
import utn.dds.fuentes.dinamica.controllers.ControllerHechoDinamica;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.config.AppConfig;

import java.net.MalformedURLException;


public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @OpenApi(
        summary = "Health check del servicio fuentes dinámicas",
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
        summary = "Información del servicio fuentes dinámicas",
        operationId = "infoServicio",
        path = "/",
        methods = HttpMethod.GET,
        tags = {"Información"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del servicio")
        }
    )
    private static void infoServicio(io.javalin.http.Context ctx) {
        ctx.result("Fuentes Dinámicas - MetaMapa");
    }
    
    @OpenApi(
        summary = "Obtener datos dinámicos disponibles",
        operationId = "obtenerDatos",
        path = "/datos",
        methods = HttpMethod.GET,
        tags = {"Datos"},
        responses = {
            @OpenApiResponse(status = "200", description = "Datos dinámicos disponibles")
        }
    )
    private static void obtenerDatos(io.javalin.http.Context ctx) {
        ctx.json(new RespuestaDatos("Datos dinámicos disponibles"));
    }

    public static void main(String[] args) throws MalformedURLException {
        AppConfig appConfig = AppConfig.fromEnvironment();

        logger.info("Iniciando servicio fuente dinamica con configuración:");
        logger.info("  - DAO Type: {}", appConfig.getDaoType());
        logger.info("  - Processor Type: {}", appConfig.getProcessorType());
        logger.info("  - API Endpoint: {}", appConfig.getApiEndpoint());

        ControllerHechoDinamica controller = new ControllerHechoDinamica(appConfig.getDaoType(), appConfig.getDaoConfig());

        Javalin app = Javalin.create(config -> {
            config.bundledPlugins.enableDevLogging();
            config.bundledPlugins.enableCors(cors -> {
                cors.addRule(it -> {
                    it.anyHost();
                });
            });
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
                                    .title("MetaMapa - Fuentes Dinámicas API")
                                    .version("1.0.0")
                                    .description("API para acceso a fuentes de datos dinámicas en MetaMapa");
                            });
                            // No especificamos servidor - Swagger UI usará la URL actual automáticamente
                    });
            }));
            
            // Registrar Swagger Plugin
            config.registerPlugin(new SwaggerPlugin());
            
            // Registrar ReDoc Plugin
            config.registerPlugin(new ReDocPlugin());
            
        });
        
        app.get("/health", Main::healthCheck);
        app.get("/", Main::infoServicio);
        
        // Endpoint para obtener datos dinámicos
        app.get("/datos", Main::obtenerDatos);

        // Pedimos los hechos
        app.get("/hechos", controller::obtenerHechos);

        // Agregamos un hecho
        app.post("/hechos", controller::agregarHecho);

        app.start(7002);
        
        logger.info("Servicio de fuentes dinámicas iniciado en puerto 7002");
    }
    
    public static class RespuestaDatos {
        public String mensaje;
        
        public RespuestaDatos(String mensaje) {
            this.mensaje = mensaje;
        }
    }
} 

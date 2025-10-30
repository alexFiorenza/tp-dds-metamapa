package utn.dds.metamapa.controller.routers;

import io.javalin.Javalin;
import io.javalin.openapi.*;
import utn.dds.metamapa.config.AppConfig;
import utn.dds.metamapa.controller.ControllerHechoPublico;
import utn.dds.metamapa.controller.ControllerSolicitudPublico;
import utn.dds.metamapa.controller.ControllerColeccionPublica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApiRoutes {
    private static final Logger logger = LoggerFactory.getLogger(ApiRoutes.class);
    private final AppConfig appConfig;
    private final ControllerHechoPublico controllerHecho;
    private final ControllerSolicitudPublico controllerSolicitud;
    private final ControllerColeccionPublica controllerColeccion;

    public ApiRoutes(AppConfig appConfig) {
        this.appConfig = appConfig;
        this.controllerHecho = new ControllerHechoPublico(
            appConfig.getDaoType(), appConfig.getDaoConfig());
        this.controllerSolicitud = new ControllerSolicitudPublico(
            appConfig.getDaoType(), appConfig.getDaoConfig());
        this.controllerColeccion = new ControllerColeccionPublica(
            appConfig.getDaoType(), appConfig.getDaoConfig());
    }

    @OpenApi(
        summary = "Información de la API pública",
        operationId = "apiInfo",
        path = "/api/",
        methods = HttpMethod.GET,
        tags = {"API Pública"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información de la API")
        }
    )
    private static void apiInfo(io.javalin.http.Context ctx) {
        ctx.result("MetaMapa API - Endpoints públicos");
    }

    @OpenApi(
        summary = "Health check de la API pública",
        operationId = "apiHealthCheck",
        path = "/api/health",
        methods = HttpMethod.GET,
        tags = {"API Pública"},
        responses = {
            @OpenApiResponse(status = "200", description = "API funcionando correctamente")
        }
    )
    private static void apiHealthCheck(io.javalin.http.Context ctx) {
        ctx.result("API OK");
    }

    public void configure(Javalin app) {
        logger.info("Configurando rutas de API...");

        // Rutas base de la API
        app.get("/api/", ApiRoutes::apiInfo);
        app.get("/api/health", ApiRoutes::apiHealthCheck);

        // Endpoints de Hechos públicos
        app.get("/api/hechos", controllerHecho::obtenerHechos);
        app.post("/api/hechos/{uuid}/reportar", controllerHecho::reportarHecho);

        // Endpoints de Colecciones públicas
        app.get("/api/colecciones", controllerColeccion::obtenerColecciones);
        app.get("/api/colecciones/{identificador}", controllerColeccion::obtenerColeccion);
        app.get("/api/colecciones/{identificador}/hechos", controllerColeccion::obtenerHechosDeColeccion);

        // Endpoints de Solicitudes públicas
        app.post("/api/solicitudes", controllerSolicitud::crearSolicitudEliminacion);

        logger.info("Rutas de API pública configuradas correctamente");
        logger.info("Endpoints disponibles:");
        logger.info("GET /api/hechos - Obtener hechos con filtros");
        logger.info("POST /api/hechos/{uuid}/reportar - Reportar un hecho");
        logger.info("GET /api/colecciones - Obtener todas las colecciones");
        logger.info("GET /api/colecciones/{id} - Obtener colección por ID");
        logger.info("GET /api/colecciones/{id}/hechos - Obtener hechos de colección");
        logger.info("POST /api/solicitudes - Crear solicitud de eliminación");
    }
}
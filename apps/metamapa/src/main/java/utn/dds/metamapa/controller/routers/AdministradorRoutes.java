package utn.dds.metamapa.controller.routers;

import io.javalin.Javalin;
import io.javalin.openapi.*;
import utn.dds.metamapa.auth.ClerkAuthHandler;
import utn.dds.metamapa.config.AppConfig;
import utn.dds.metamapa.controller.ControllerColeccionAdministrativo;
import utn.dds.metamapa.controller.ControllerSolicitudEliminacionAdministrativo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdministradorRoutes {
    private static final Logger logger = LoggerFactory.getLogger(AdministradorRoutes.class);
    private final ControllerColeccionAdministrativo controllerColeccion;
    private final ControllerSolicitudEliminacionAdministrativo controllerSolicitud;
    private final ClerkAuthHandler authHandler;

    public AdministradorRoutes(AppConfig appConfig) {
        this.controllerColeccion = new ControllerColeccionAdministrativo(
            appConfig.getDaoType(), appConfig.getDaoConfig());
        this.controllerSolicitud = new ControllerSolicitudEliminacionAdministrativo(
            appConfig.getDaoType(), appConfig.getDaoConfig());

        // Inicializar autenticación con Clerk (la secret key es obligatoria en AppConfig)
        logger.info("Configurando autenticación Clerk con SDK oficial");
        this.authHandler = new ClerkAuthHandler(appConfig.getClerkSecretKey(), true); // Requiere rol de admin
    }

    @OpenApi(
        summary = "Panel de control del administrador",
        operationId = "administradorInfo",
        path = "/administrador/",
        methods = HttpMethod.GET,
        tags = {"Administrador"},
        security = {@OpenApiSecurity(name = "BearerAuth")},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del panel de administrador"),
            @OpenApiResponse(status = "401", description = "No autenticado"),
            @OpenApiResponse(status = "403", description = "No tiene permisos de administrador")
        }
    )
    private static void administradorInfo(io.javalin.http.Context ctx) {
        ctx.result("MetaMapa Administrador - Panel de control");
    }

    @OpenApi(
        summary = "Health check del administrador",
        operationId = "administradorHealthCheck",
        path = "/administrador/health",
        methods = HttpMethod.GET,
        tags = {"Administrador"},
        security = {@OpenApiSecurity(name = "BearerAuth")},
        responses = {
            @OpenApiResponse(status = "200", description = "Administrador funcionando correctamente"),
            @OpenApiResponse(status = "401", description = "No autenticado"),
            @OpenApiResponse(status = "403", description = "No tiene permisos de administrador")
        }
    )
    private static void administradorHealthCheck(io.javalin.http.Context ctx) {
        ctx.result("Administrador OK");
    }

    public void configure(Javalin app) {
        logger.info("Configurando rutas de administrador...");

        // Aplicar autenticación a todas las rutas de administrador
        app.before("/administrador/*", authHandler);
        logger.info("Filtro de autenticación Clerk aplicado a rutas de administrador");

        // Rutas base del administrador
        app.get("/administrador/", AdministradorRoutes::administradorInfo);
        app.get("/administrador/health", AdministradorRoutes::administradorHealthCheck);

        // Rutas CRUD para colecciones
        app.get("/administrador/coleccion", controllerColeccion::obtenerColecciones);
        app.get("/administrador/coleccion/{id}", controllerColeccion::obtenerColeccionPorId);
        app.post("/administrador/coleccion", controllerColeccion::crearColeccion);
        app.put("/administrador/coleccion/{id}", controllerColeccion::actualizarColeccion);
        app.delete("/administrador/coleccion/{id}", controllerColeccion::eliminarColeccion);

        // Rutas para hechos de colecciones
        app.get("/administrador/coleccion/{id}/hechos", controllerColeccion::obtenerHechosDeColeccion);

        // Rutas administrativas para solicitudes de eliminación
        app.get("/administrador/solicitudes", controllerSolicitud::obtenerSolicitudes);
        app.put("/administrador/solicitud/{uuid}/aceptar", controllerSolicitud::aceptarSolicitud);
        app.put("/administrador/solicitud/{uuid}/rechazar", controllerSolicitud::rechazarSolicitud);

        logger.info("Rutas de administrador configuradas correctamente");
    }
}
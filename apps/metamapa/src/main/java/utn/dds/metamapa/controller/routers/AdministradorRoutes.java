package utn.dds.metamapa.controller;

import io.javalin.Javalin;
import io.javalin.openapi.*;
import utn.dds.metamapa.config.AppConfig;
import utn.dds.metamapa.controller.ControllerColeccionAdministrativo;
import utn.dds.metamapa.controller.ControllerSolicitudEliminacionAdministrativo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdministradorRoutes {
    private static final Logger logger = LoggerFactory.getLogger(AdministradorRoutes.class);
    private final ControllerColeccionAdministrativo controllerColeccion;
    private final ControllerSolicitudEliminacionAdministrativo controllerSolicitud;

    public AdministradorRoutes(AppConfig appConfig) {
        this.controllerColeccion = new ControllerColeccionAdministrativo(
            appConfig.getDaoType(), appConfig.getDaoConfig());
        this.controllerSolicitud = new ControllerSolicitudEliminacionAdministrativo(
            appConfig.getDaoType(), appConfig.getDaoConfig());
    }

    @OpenApi(
        summary = "Panel de control del administrador",
        operationId = "administradorInfo",
        path = "/administrador/",
        methods = HttpMethod.GET,
        tags = {"Administrador"},
        responses = {
            @OpenApiResponse(status = "200", description = "Información del panel de administrador")
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
        responses = {
            @OpenApiResponse(status = "200", description = "Administrador funcionando correctamente")
        }
    )
    private static void administradorHealthCheck(io.javalin.http.Context ctx) {
        ctx.result("Administrador OK");
    }

    public void configure(Javalin app) {
        logger.info("Configurando rutas de administrador...");

        // Rutas base del administrador
        app.get("/administrador/", AdministradorRoutes::administradorInfo);
        app.get("/administrador/health", AdministradorRoutes::administradorHealthCheck);

        // Rutas CRUD para colecciones
        app.get("/administrador/coleccion", controllerColeccion::obtenerColecciones);
        app.get("/administrador/coleccion/{id}", controllerColeccion::obtenerColeccionPorId);
        app.post("/administrador/coleccion", controllerColeccion::crearColeccion);
        app.put("/administrador/coleccion/{id}", controllerColeccion::actualizarColeccion);
        app.delete("/administrador/coleccion/{id}", controllerColeccion::eliminarColeccion);
        app.get("/administrador/coleccion/{id}/hechos", controllerColeccion::buscarHechosEnColeccion);

        // Rutas administrativas para solicitudes de eliminación
        app.put("/administrador/solicitud/{uuid}/aceptar", controllerSolicitud::aceptarSolicitud);
        app.put("/administrador/solicitud/{uuid}/rechazar", controllerSolicitud::rechazarSolicitud);

        logger.info("Rutas de administrador configuradas correctamente");
    }
}
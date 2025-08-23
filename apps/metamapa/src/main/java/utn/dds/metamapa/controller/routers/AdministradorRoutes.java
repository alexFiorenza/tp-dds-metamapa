package utn.dds.metamapa.controller;

import io.javalin.Javalin;
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

    public void configure(Javalin app) {
        logger.info("Configurando rutas de administrador...");
        
        // Rutas base del administrador
        app.get("/administrador/", ctx -> ctx.result("MetaMapa Administrador - Panel de control"));
        app.get("/administrador/health", ctx -> ctx.result("Administrador OK"));
        
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
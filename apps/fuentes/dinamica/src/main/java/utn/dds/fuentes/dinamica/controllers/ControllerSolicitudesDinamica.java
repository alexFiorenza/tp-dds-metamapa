package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.fuentes.dinamica.Main;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import utn.dds.fuentes.dinamica.services.ServiceSolicitudesDinamica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerSolicitudesDinamica {
    private final ServiceSolicitudesDinamica solicitudesService;
    private static final Logger loggerSolicitudes = LoggerFactory.getLogger(ControllerSolicitudesDinamica.class);

    public ControllerSolicitudesDinamica(String daoType, Map<String, Object> daoConfig){
        this.solicitudesService = new ServiceSolicitudesDinamica(daoType, daoConfig);
    }

    public void obtenerSolicitudesDeEliminacion(Context ctx){
        try {
            loggerSolicitudes.info("-----------Obteniendo Solicitudes de Eliminación-----------");
            String paginaParam = ctx.queryParam("pagina");
            String tamanioParam = ctx.queryParam("tamanio");

            // Siempre usar paginación, con valores por defecto si no se especifican
            int pagina = paginaParam != null ? Integer.parseInt(paginaParam) : 0;
            int tamanio = tamanioParam != null ? Integer.parseInt(tamanioParam) : 10;

            RespuestaPaginadaDTO<SolicitudEliminacion> respuestaPaginada = solicitudesService.obtenerSolicitudesPaginadas(pagina, tamanio);

            // Convertir las Solicitudes a DTO
            List<SolicitudEliminacionDTO> SolicitudesDTO = respuestaPaginada.getDatos().stream()
                    .map(SolicitudEliminacionDTO::fromSolicitudEliminacion)
                    .collect(Collectors.toList());


            // Crear respuesta paginada con DTOs
            RespuestaPaginadaDTO<SolicitudEliminacionDTO> respuestaDTOPaginada = new RespuestaPaginadaDTO<>(
                    SolicitudesDTO,
                    respuestaPaginada.getPagina(),
                    respuestaPaginada.getTamanioPagina(),
                    respuestaPaginada.getTotalElementos()
            );

            ctx.json(respuestaDTOPaginada);
        } catch (NumberFormatException e) {
            ctx.status(400).result("Error en parámetros de paginación: " + e.getMessage());
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }

    public void agregarSolicitudDeEliminacionDeHecho(Context ctx) {
        try {
            loggerSolicitudes.info("-----------Agregando Solicitud de Eliminación-----------");
            SolicitudEliminacionDTO solicitudDTO = ctx.bodyAsClass(SolicitudEliminacionDTO.class);
            SolicitudEliminacion solicitud = solicitudDTO.toSolicitudEliminacion();
            this.solicitudesService.agregarSolicitud(solicitud);
            ctx.json(solicitudDTO);
            loggerSolicitudes.info("Se agrego exitosamente la solicitud de eliminación");
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar solicitud de eliminacion: " + e.getMessage());
        }
    }


    /////////////////////////////////////////////////////////////////////////////////////////////////
    ////// esta parte seria manejada por un administrador, asi que no se si va en controller  ///////
    /////////////////////////////////////////////////////////////////////////////////////////////////

    public void aceptarSolicitud(String uuid, Context ctx){
        try {
            this.solicitudesService.aceptarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al aceptar solicitud: " + e.getMessage());
        }
    }

    public void rechazarSolicitud(String uuid, Context ctx){
        try {
            this.solicitudesService.rechazarSolicitud(uuid);
            ctx.status(200);
        } catch (Exception e) {
            ctx.status(500).result("Error al rechazar solicitud: " + e.getMessage());
        }
    }

}


package utn.dds.metamapa.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.metamapa.persistencia.SolicitudEliminacionRepository;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.Map;

public class ServiceSolicitudEliminacion {
    private static final Logger logger = LoggerFactory.getLogger(ServiceSolicitudEliminacion.class);

    private final SolicitudEliminacionRepository solicitudEliminacionRepository;
    private final ServiceHechoMetamapa serviceHechoMetamapa;

    public ServiceSolicitudEliminacion(String daoType, Map<String, Object> daoConfig) {
        this.solicitudEliminacionRepository = new SolicitudEliminacionRepository(daoConfig);
        this.serviceHechoMetamapa = new ServiceHechoMetamapa(daoType, daoConfig);
    }

    public void crearSolicitud(SolicitudEliminacion solicitud) {
        this.solicitudEliminacionRepository.crear(solicitud);
    }

    public void aceptar(String uuid) {
        SolicitudEliminacion solicitud = this.solicitudEliminacionRepository.obtenerPorId(uuid);
        if (solicitud == null) {
            throw new RuntimeException("Solicitud no encontrada");
        }

        try {
            // Ocultar el hecho asociado
            String hechoUuid = solicitud.getHecho();
            logger.info("Ocultando hecho {} por solicitud de eliminación aceptada {}", hechoUuid, uuid);
            this.serviceHechoMetamapa.reportarHecho(hechoUuid);

            // Marcar la solicitud como procesada (oculta)
            solicitud.ocultar();
            this.solicitudEliminacionRepository.actualizar(uuid, solicitud);

            logger.info("Solicitud de eliminación {} aceptada exitosamente", uuid);
        } catch (Exception e) {
            logger.error("Error al aceptar solicitud de eliminación {}: {}", uuid, e.getMessage());
            throw new RuntimeException("Error al procesar solicitud de eliminación: " + e.getMessage(), e);
        }
    }

    public void rechazar(String uuid) {
        SolicitudEliminacion solicitud = this.solicitudEliminacionRepository.obtenerPorId(uuid);
        if (solicitud == null) {
            throw new RuntimeException("Solicitud no encontrada");
        }
        solicitud.ocultar();
        this.solicitudEliminacionRepository.actualizar(uuid, solicitud);
    }

    public RespuestaPaginadaDTO<SolicitudEliminacionDTO> obtenerSolicitudes(int page, int size) {
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10; // Máximo 100 elementos por página

        return this.solicitudEliminacionRepository.obtenerTodos(page, size);
    }
}
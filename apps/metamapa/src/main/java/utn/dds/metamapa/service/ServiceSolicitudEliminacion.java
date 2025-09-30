package utn.dds.metamapa.service;

import utn.dds.metamapa.persistencia.SolicitudEliminacionRepository;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.Map;

public class ServiceSolicitudEliminacion {
    private final SolicitudEliminacionRepository solicitudEliminacionRepository;

    public ServiceSolicitudEliminacion(String daoType, Map<String, Object> daoConfig) {
        this.solicitudEliminacionRepository = new SolicitudEliminacionRepository(daoConfig);
    }

    public void crearSolicitud(SolicitudEliminacion solicitud) {
        this.solicitudEliminacionRepository.crear(solicitud);
    }

    public void aceptar(String uuid) {
        SolicitudEliminacion solicitud = this.solicitudEliminacionRepository.obtenerPorId(uuid);
        if (solicitud == null) {
            throw new RuntimeException("Solicitud no encontrada");
        }
        solicitud.activar();
        this.solicitudEliminacionRepository.actualizar(uuid, solicitud);
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
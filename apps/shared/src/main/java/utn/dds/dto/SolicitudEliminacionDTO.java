package utn.dds.dto;

import utn.dds.dominio.EstadoSolicitud;
import utn.dds.dominio.SolicitudEliminacion;

import java.time.LocalDateTime;

public class SolicitudEliminacionDTO {
    private String texto;
    private String hecho;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;
    private final String uuid;

    // Constructor vacío para deserialización
    public SolicitudEliminacionDTO() {
        this.uuid = null;
    }

    // Constructor completo
    public SolicitudEliminacionDTO(String texto, String hecho, LocalDateTime fechaSolicitud, EstadoSolicitud estado, String uuid) {
        this.texto = texto;
        this.hecho = hecho;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.uuid = uuid;
    }

    // Método estático para crear DTO desde entidad de dominio
    public static SolicitudEliminacionDTO fromSolicitudEliminacion(SolicitudEliminacion solicitud) {
        return new SolicitudEliminacionDTO(
            solicitud.getTexto(),
            solicitud.getHecho(),
            solicitud.getFechaSolicitud(),
            solicitud.getEstado(),
            solicitud.getUuid()
        );
    }

    // Método para convertir DTO a entidad de dominio
    public SolicitudEliminacion toSolicitudEliminacion() {
        return new SolicitudEliminacion(
            this.texto,
            this.hecho,
            this.fechaSolicitud,
            this.estado,
            this.uuid
        );
    }

    // Getters
    public String getTexto() {
        return texto;
    }

    public String getHecho() {
        return hecho;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public String getUuid() {
        return uuid;
    }

    // Setters
    public void setTexto(String texto) {
        this.texto = texto;
    }

    public void setHecho(String hecho) {
        this.hecho = hecho;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
}

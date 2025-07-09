package utn.dds.dto;

import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoSolicitud;

import java.time.LocalDateTime;

public class SolicitudEliminacionDTO {
    private String uuid;
    private String texto;
    private String hecho;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;

    // Constructor vacío para deserialización
    public SolicitudEliminacionDTO() {}

    // Constructor completo
    public SolicitudEliminacionDTO(String uuid, String texto, String hecho, LocalDateTime fechaSolicitud, EstadoSolicitud estado) {
        this.uuid = uuid;
        this.texto = texto;
        this.hecho = hecho;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
    }

    // Método estático para crear DTO desde entidad de dominio
    public static SolicitudEliminacionDTO fromSolicitudEliminacion(SolicitudEliminacion solicitud) {
        return new SolicitudEliminacionDTO(
            solicitud.getUuid(),
            solicitud.getTexto(),
            solicitud.getHecho(),
            solicitud.getFechaSolicitud(),
            solicitud.getEstado()
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

    // Getters y Setters
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public String getHecho() {
        return hecho;
    }

    public void setHecho(String hecho) {
        this.hecho = hecho;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
}
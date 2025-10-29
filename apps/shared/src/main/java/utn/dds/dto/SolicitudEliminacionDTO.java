package utn.dds.dto;

import utn.dds.dominio.EstadoSolicitud;
import utn.dds.dominio.SolicitudEliminacion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SolicitudEliminacionDTO {
    private String texto;
    private String hecho;
    private String fechaSolicitud; // ISO-8601: "2025-10-29T22:54:58"
    private EstadoSolicitud estado;
    private final String uuid;

    // Constructor vacío para deserialización
    public SolicitudEliminacionDTO() {
        this.uuid = null;
    }

    // Constructor completo
    public SolicitudEliminacionDTO(String texto, String hecho, String fechaSolicitud, EstadoSolicitud estado, String uuid) {
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
            solicitud.getFechaSolicitud() != null ? solicitud.getFechaSolicitud().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
            solicitud.getEstado(),
            solicitud.getUuid()
        );
    }

    // Método para convertir DTO a entidad de dominio
    public SolicitudEliminacion toSolicitudEliminacion() {
        return new SolicitudEliminacion(
            this.texto,
            this.hecho,
            this.fechaSolicitud != null ? LocalDateTime.parse(this.fechaSolicitud, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
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

    public String getFechaSolicitud() {
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

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
}

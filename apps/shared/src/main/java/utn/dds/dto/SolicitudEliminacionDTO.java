package utn.dds.dto;

import utn.dds.dominio.EstadoSolicitud;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.Hecho;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SolicitudEliminacionDTO {
    private String texto;
    private String hecho; // UUID del hecho (mantener para compatibilidad)
    private HechoDTO hechoDTO; // Hecho completo populado
    private String fechaSolicitud; // ISO-8601: "2025-10-29T22:54:58"
    private EstadoSolicitud estado;
    private final String uuid;

    // Constructor vacío para deserialización
    public SolicitudEliminacionDTO() {
        this.uuid = null;
    }

    // Constructor completo
    public SolicitudEliminacionDTO(String texto, String hecho, HechoDTO hechoDTO, String fechaSolicitud, EstadoSolicitud estado, String uuid) {
        this.texto = texto;
        this.hecho = hecho;
        this.hechoDTO = hechoDTO;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.uuid = uuid;
    }

    // Constructor sin HechoDTO (para compatibilidad)
    public SolicitudEliminacionDTO(String texto, String hecho, String fechaSolicitud, EstadoSolicitud estado, String uuid) {
        this(texto, hecho, null, fechaSolicitud, estado, uuid);
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

    public HechoDTO getHechoDTO() {
        return hechoDTO;
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

    public void setHechoDTO(HechoDTO hechoDTO) {
        this.hechoDTO = hechoDTO;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
}

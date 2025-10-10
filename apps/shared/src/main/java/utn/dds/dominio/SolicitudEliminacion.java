package utn.dds.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_eliminacion")
public class SolicitudEliminacion {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "texto", nullable = false, columnDefinition = "TEXT")
    private String texto;

    @Column(name = "hecho", nullable = false)
    private String hecho;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoSolicitud estado;

    public SolicitudEliminacion() {
    }

    // Constructor
    public SolicitudEliminacion(String texto, String hecho, LocalDateTime fechaSolicitud,
                                EstadoSolicitud estado,  String uuid) {
        this.texto = texto;
        this.hecho = hecho;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.uuid = uuid;
    }

    // GETTERS
    public String getTexto() {
        return texto;
    }

    public String getHecho() {
        return hecho;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    // SETTERS
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

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

    // ACCIONES
    public void ocultar() {
        this.estado = EstadoSolicitud.OCULTO;
    }

    public void activar() {
        this.estado = EstadoSolicitud.ACTIVO;
    }
} 
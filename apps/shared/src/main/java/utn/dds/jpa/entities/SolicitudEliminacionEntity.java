package utn.dds.jpa.entities;

import jakarta.persistence.*;
import utn.dds.dominio.EstadoSolicitud;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_eliminacion")
public class SolicitudEliminacionEntity {

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

    public SolicitudEliminacionEntity() {
    }

    // Getters
    public String getUuid() {
        return uuid;
    }

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

    // Setters
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
}
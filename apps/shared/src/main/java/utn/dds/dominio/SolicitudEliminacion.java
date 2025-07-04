package utn.dds.dominio;

import java.time.LocalDateTime;

public class SolicitudEliminacion {

    private final String texto;
    private final String hecho;
    private final LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;
    private final String uuid;

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

    public void ocultar() {
        this.estado = EstadoSolicitud.OCULTO;
    }

    public void activar() {
        this.estado = EstadoSolicitud.ACTIVO;
    }
} 
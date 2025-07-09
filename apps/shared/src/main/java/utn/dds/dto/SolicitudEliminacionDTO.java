package utn.dds.dto;

import utn.dds.dominio.EstadoSolicitud;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;

import java.time.LocalDateTime;

public class SolicitudEliminacionDTO {
    private String texto;
    private String hecho;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;
    private final String uuid;

    public SolicitudEliminacionDTO(String texto, String hecho, LocalDateTime fechaSolicitud, EstadoSolicitud estado, String uuid) {
        this.texto = texto;
        this.hecho = hecho;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.uuid = uuid;
    }

    public static SolicitudEliminacionDTO fromSolicitudEliminacion(SolicitudEliminacion solicitud){
        return new SolicitudEliminacionDTO(solicitud.getTexto(), solicitud.getHecho(), solicitud.getFechaSolicitud(), solicitud.getEstado(), solicitud.getUuid());
    }

    public SolicitudEliminacion toSolicitudEliminacion(){
        return new SolicitudEliminacion(this.texto, this.hecho, this.fechaSolicitud, this.estado,this.uuid);
    }

    // Getters
    public String getTexto() { return texto; };
    public String getHecho() { return hecho; };
    public LocalDateTime getFechaSolicitud() { return fechaSolicitud; };
    public EstadoSolicitud getEstado() { return estado; };
    public String getUuid() { return uuid;};

    public void setTexto(String nuevoTexto){ this.texto = nuevoTexto;};
    public void setHecho(String nuevoHecho){ this.hecho = nuevoHecho;};
    public void setFechaSolicitud (LocalDateTime nuevaFecha) { this.fechaSolicitud = nuevaFecha;};
    public void setEstado(EstadoSolicitud nuevoEstado) { this.estado = nuevoEstado;};

}

package utn.dds.model;

import java.time.LocalDateTime;

public class SolicitudEliminacion {

    private String texto;
    private Hecho hecho;
    private LocalDateTime fechaSolicitud;
    private EstadoSolicitud estado;     // Hay que crear esta clase
    private DetectorSpam detectorSpam;  // Hay que crear esta clase

    // Constructor
    public SolicitudEliminacion(String texto, Hecho hecho, LocalDateTime fechaSolicitud,
                                EstadoSolicitud estado, DetectorSpam detectorSpam) {
        this.texto = texto;
        this.hecho = hecho;
        this.fechaSolicitud = fechaSolicitud;
        this.estado = estado;
        this.detectorSpam = detectorSpam;
    }

    public void ocultar() {
        this.estado = EstadoHecho.OCULTO;
    }

    public void activar() {
        this.estado = EstadoHecho.ACTIVO;
    }
}

public enum EstadoHecho {
    ACTIVO,
    OCULTO;
}
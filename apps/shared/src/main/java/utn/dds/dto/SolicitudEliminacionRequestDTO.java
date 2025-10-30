package utn.dds.dto;

/**
 * DTO para requests de creación de solicitudes de eliminación (sin UUID, fecha ni estado)
 */
public class SolicitudEliminacionRequestDTO {
    private String texto;
    private String hecho;

    public SolicitudEliminacionRequestDTO() {}

    public SolicitudEliminacionRequestDTO(String texto, String hecho) {
        this.texto = texto;
        this.hecho = hecho;
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
}
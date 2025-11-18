package utn.dds.dto;

/**
 * DTO para representar un contribuyente en HechoDTO
 */
public class ContribuyenteDTO {
    private String nombre;
    private String userId;

    public ContribuyenteDTO() {}

    public ContribuyenteDTO(String nombre, String userId) {
        this.nombre = nombre;
        this.userId = userId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}


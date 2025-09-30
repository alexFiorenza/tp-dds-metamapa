package utn.dds.dto;

import java.util.List;

public class ColeccionDTO {
    private String handle;
    private String titulo;
    private String descripcion;
    private List<FuenteDTO> fuentes;
    private List<CriterioCreateDTO> criteriosDePertenencia;

    public ColeccionDTO() {}

    public ColeccionDTO(String handle, String titulo, String descripcion,
                       List<FuenteDTO> fuentes,
                       List<CriterioCreateDTO> criteriosDePertenencia) {
        this.handle = handle;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fuentes = fuentes;
        this.criteriosDePertenencia = criteriosDePertenencia;
    }

    // Getters
    public String getHandle() {
        return handle;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<FuenteDTO> getFuentes() {
        return fuentes;
    }

    public List<CriterioCreateDTO> getCriteriosDePertenencia() {
        return criteriosDePertenencia;
    }

    // Setters
    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFuentes(List<FuenteDTO> fuentes) {
        this.fuentes = fuentes;
    }

    public void setCriteriosDePertenencia(List<CriterioCreateDTO> criteriosDePertenencia) {
        this.criteriosDePertenencia = criteriosDePertenencia;
    }
}
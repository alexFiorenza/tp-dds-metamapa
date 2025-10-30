package utn.dds.dto;

import java.util.List;

public class ColeccionUpdateDTO {
    private String titulo;
    private String descripcion;
    private List<String> fuentesIds;
    private List<CriterioCreateDTO> criteriosDePertenencia;

    public ColeccionUpdateDTO() {}

    public ColeccionUpdateDTO(String titulo, String descripcion, List<String> fuentesIds, List<CriterioCreateDTO> criteriosDePertenencia) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fuentesIds = fuentesIds;
        this.criteriosDePertenencia = criteriosDePertenencia;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public List<String> getFuentesIds() {
        return fuentesIds;
    }

    public void setFuentesIds(List<String> fuentesIds) {
        this.fuentesIds = fuentesIds;
    }

    public List<CriterioCreateDTO> getCriteriosDePertenencia() {
        return criteriosDePertenencia;
    }

    public void setCriteriosDePertenencia(List<CriterioCreateDTO> criteriosDePertenencia) {
        this.criteriosDePertenencia = criteriosDePertenencia;
    }
}
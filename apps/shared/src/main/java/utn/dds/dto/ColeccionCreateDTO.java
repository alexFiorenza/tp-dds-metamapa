package utn.dds.dto;

import java.util.List;

public class ColeccionCreateDTO {
    private String titulo;
    private String descripcion;
    private List<String> fuentesIds;
    private List<CriterioCreateDTO> criteriosDePertenencia;
    private String algoritmoConsenso; // String: "menciones", "simple", "absoluta", "default"

    public ColeccionCreateDTO() {}

    public ColeccionCreateDTO(String titulo, String descripcion, List<String> fuentesIds, 
                              List<CriterioCreateDTO> criteriosDePertenencia, 
                              String algoritmoConsenso) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fuentesIds = fuentesIds;
        this.criteriosDePertenencia = criteriosDePertenencia;
        this.algoritmoConsenso = algoritmoConsenso;
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

    public String getAlgoritmoConsenso() {
        return algoritmoConsenso;
    }

    public void setAlgoritmoConsenso(String algoritmoConsenso) {
        this.algoritmoConsenso = algoritmoConsenso;
    }
}
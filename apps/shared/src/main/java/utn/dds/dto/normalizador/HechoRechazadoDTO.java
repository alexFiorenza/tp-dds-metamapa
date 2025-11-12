package utn.dds.dto.normalizador;

import com.fasterxml.jackson.annotation.JsonProperty;
import utn.dds.dto.HechoDTO;

import java.util.List;

public class HechoRechazadoDTO {

    @JsonProperty("hechoOriginal")
    private HechoDTO hechoOriginal;

    @JsonProperty("motivo")
    private String motivo;

    @JsonProperty("errores")
    private List<String> errores;

    // Constructor vacío
    public HechoRechazadoDTO() {
    }

    // Constructor con parámetros
    public HechoRechazadoDTO(HechoDTO hechoOriginal, String motivo, List<String> errores) {
        this.hechoOriginal = hechoOriginal;
        this.motivo = motivo;
        this.errores = errores;
    }

    // Getters y Setters
    public HechoDTO getHechoOriginal() {
        return hechoOriginal;
    }

    public void setHechoOriginal(HechoDTO hechoOriginal) {
        this.hechoOriginal = hechoOriginal;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void setErrores(List<String> errores) {
        this.errores = errores;
    }
}

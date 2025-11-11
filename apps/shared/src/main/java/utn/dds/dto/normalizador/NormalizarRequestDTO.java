package utn.dds.dto.normalizador;

import com.fasterxml.jackson.annotation.JsonProperty;
import utn.dds.dto.HechoDTO;

import java.util.List;

public class NormalizarRequestDTO {

    @JsonProperty("hechos")
    private List<HechoDTO> hechos;

    @JsonProperty("config")
    private ConfiguracionNormalizacionDTO config;

    // Constructor vacío
    public NormalizarRequestDTO() {
    }

    // Constructor con parámetros
    public NormalizarRequestDTO(List<HechoDTO> hechos, ConfiguracionNormalizacionDTO config) {
        this.hechos = hechos;
        this.config = config;
    }

    // Getters y Setters
    public List<HechoDTO> getHechos() {
        return hechos;
    }

    public void setHechos(List<HechoDTO> hechos) {
        this.hechos = hechos;
    }

    public ConfiguracionNormalizacionDTO getConfig() {
        return config;
    }

    public void setConfig(ConfiguracionNormalizacionDTO config) {
        this.config = config;
    }
}

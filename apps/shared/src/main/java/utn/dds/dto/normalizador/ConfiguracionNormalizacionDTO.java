package utn.dds.dto.normalizador;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ConfiguracionNormalizacionDTO {

    @JsonProperty("normalizarCategorias")
    private Boolean normalizarCategorias;

    @JsonProperty("normalizarFechas")
    private Boolean normalizarFechas;

    @JsonProperty("normalizarCoordenadas")
    private Boolean normalizarCoordenadas;

    @JsonProperty("normalizarTexto")
    private Boolean normalizarTexto;

    @JsonProperty("normalizarEtiquetas")
    private Boolean normalizarEtiquetas;

    @JsonProperty("usarFuzzyMatching")
    private Boolean usarFuzzyMatching;

    @JsonProperty("umbralSimilitud")
    private Double umbralSimilitud;

    @JsonProperty("rechazarCoordenadasInvalidas")
    private Boolean rechazarCoordenadasInvalidas;

    // Constructor vacío
    public ConfiguracionNormalizacionDTO() {
    }

    // Constructor con valores por defecto
    public static ConfiguracionNormalizacionDTO porDefecto() {
        ConfiguracionNormalizacionDTO config = new ConfiguracionNormalizacionDTO();
        config.normalizarCategorias = true;
        config.normalizarFechas = true;
        config.normalizarCoordenadas = true;
        config.normalizarTexto = true;
        config.normalizarEtiquetas = true;
        config.usarFuzzyMatching = true;
        config.umbralSimilitud = 0.85;
        config.rechazarCoordenadasInvalidas = true;
        return config;
    }

    // Getters y Setters
    public Boolean getNormalizarCategorias() {
        return normalizarCategorias;
    }

    public void setNormalizarCategorias(Boolean normalizarCategorias) {
        this.normalizarCategorias = normalizarCategorias;
    }

    public Boolean getNormalizarFechas() {
        return normalizarFechas;
    }

    public void setNormalizarFechas(Boolean normalizarFechas) {
        this.normalizarFechas = normalizarFechas;
    }

    public Boolean getNormalizarCoordenadas() {
        return normalizarCoordenadas;
    }

    public void setNormalizarCoordenadas(Boolean normalizarCoordenadas) {
        this.normalizarCoordenadas = normalizarCoordenadas;
    }

    public Boolean getNormalizarTexto() {
        return normalizarTexto;
    }

    public void setNormalizarTexto(Boolean normalizarTexto) {
        this.normalizarTexto = normalizarTexto;
    }

    public Boolean getNormalizarEtiquetas() {
        return normalizarEtiquetas;
    }

    public void setNormalizarEtiquetas(Boolean normalizarEtiquetas) {
        this.normalizarEtiquetas = normalizarEtiquetas;
    }

    public Boolean getUsarFuzzyMatching() {
        return usarFuzzyMatching;
    }

    public void setUsarFuzzyMatching(Boolean usarFuzzyMatching) {
        this.usarFuzzyMatching = usarFuzzyMatching;
    }

    public Double getUmbralSimilitud() {
        return umbralSimilitud;
    }

    public void setUmbralSimilitud(Double umbralSimilitud) {
        this.umbralSimilitud = umbralSimilitud;
    }

    public Boolean getRechazarCoordenadasInvalidas() {
        return rechazarCoordenadasInvalidas;
    }

    public void setRechazarCoordenadasInvalidas(Boolean rechazarCoordenadasInvalidas) {
        this.rechazarCoordenadasInvalidas = rechazarCoordenadasInvalidas;
    }
}

package utn.dds.dto.normalizador;

import com.fasterxml.jackson.annotation.JsonProperty;
import utn.dds.dto.HechoDTO;

import java.util.List;
import java.util.Map;

public class NormalizarResponseDTO {

    @JsonProperty("hechosNormalizados")
    private List<HechoDTO> hechosNormalizados;

    @JsonProperty("hechosRechazados")
    private List<HechoRechazadoDTO> hechosRechazados;

    @JsonProperty("estadisticas")
    private EstadisticasNormalizacionDTO estadisticas;

    // Constructor vacío
    public NormalizarResponseDTO() {
    }

    // Constructor con parámetros
    public NormalizarResponseDTO(List<HechoDTO> hechosNormalizados,
                                  List<HechoRechazadoDTO> hechosRechazados,
                                  EstadisticasNormalizacionDTO estadisticas) {
        this.hechosNormalizados = hechosNormalizados;
        this.hechosRechazados = hechosRechazados;
        this.estadisticas = estadisticas;
    }

    // Getters y Setters
    public List<HechoDTO> getHechosNormalizados() {
        return hechosNormalizados;
    }

    public void setHechosNormalizados(List<HechoDTO> hechosNormalizados) {
        this.hechosNormalizados = hechosNormalizados;
    }

    public List<HechoRechazadoDTO> getHechosRechazados() {
        return hechosRechazados;
    }

    public void setHechosRechazados(List<HechoRechazadoDTO> hechosRechazados) {
        this.hechosRechazados = hechosRechazados;
    }

    public EstadisticasNormalizacionDTO getEstadisticas() {
        return estadisticas;
    }

    public void setEstadisticas(EstadisticasNormalizacionDTO estadisticas) {
        this.estadisticas = estadisticas;
    }

    // Clase interna para estadísticas
    public static class EstadisticasNormalizacionDTO {
        @JsonProperty("totalProcesados")
        private Integer totalProcesados;

        @JsonProperty("totalNormalizados")
        private Integer totalNormalizados;

        @JsonProperty("totalRechazados")
        private Integer totalRechazados;

        @JsonProperty("cambiosPorTipo")
        private Map<String, Integer> cambiosPorTipo;

        @JsonProperty("tiempoEjecucion")
        private Long tiempoEjecucion;

        // Constructor vacío
        public EstadisticasNormalizacionDTO() {
        }

        // Getters y Setters
        public Integer getTotalProcesados() {
            return totalProcesados;
        }

        public void setTotalProcesados(Integer totalProcesados) {
            this.totalProcesados = totalProcesados;
        }

        public Integer getTotalNormalizados() {
            return totalNormalizados;
        }

        public void setTotalNormalizados(Integer totalNormalizados) {
            this.totalNormalizados = totalNormalizados;
        }

        public Integer getTotalRechazados() {
            return totalRechazados;
        }

        public void setTotalRechazados(Integer totalRechazados) {
            this.totalRechazados = totalRechazados;
        }

        public Map<String, Integer> getCambiosPorTipo() {
            return cambiosPorTipo;
        }

        public void setCambiosPorTipo(Map<String, Integer> cambiosPorTipo) {
            this.cambiosPorTipo = cambiosPorTipo;
        }

        public Long getTiempoEjecucion() {
            return tiempoEjecucion;
        }

        public void setTiempoEjecucion(Long tiempoEjecucion) {
            this.tiempoEjecucion = tiempoEjecucion;
        }
    }
}

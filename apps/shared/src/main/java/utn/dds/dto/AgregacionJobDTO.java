package utn.dds.dto;

import java.time.LocalDateTime;
import java.util.List;

public class AgregacionJobDTO {
    private String jobId;
    private String estado;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private Integer fuentesConsultadas;
    private Integer totalFuentes;
    private Integer hechosObtenidos;
    private Integer hechosAgregados;
    private List<String> errores;
    private String mensajeError;

    public AgregacionJobDTO() {
    }

    public AgregacionJobDTO(String jobId, String estado, LocalDateTime fechaInicio, LocalDateTime fechaFin,
                           Integer fuentesConsultadas, Integer totalFuentes, Integer hechosObtenidos,
                           Integer hechosAgregados, List<String> errores, String mensajeError) {
        this.jobId = jobId;
        this.estado = estado;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.fuentesConsultadas = fuentesConsultadas;
        this.totalFuentes = totalFuentes;
        this.hechosObtenidos = hechosObtenidos;
        this.hechosAgregados = hechosAgregados;
        this.errores = errores;
        this.mensajeError = mensajeError;
    }

    // Getters y Setters
    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public Integer getFuentesConsultadas() {
        return fuentesConsultadas;
    }

    public void setFuentesConsultadas(Integer fuentesConsultadas) {
        this.fuentesConsultadas = fuentesConsultadas;
    }

    public Integer getTotalFuentes() {
        return totalFuentes;
    }

    public void setTotalFuentes(Integer totalFuentes) {
        this.totalFuentes = totalFuentes;
    }

    public Integer getHechosObtenidos() {
        return hechosObtenidos;
    }

    public void setHechosObtenidos(Integer hechosObtenidos) {
        this.hechosObtenidos = hechosObtenidos;
    }

    public Integer getHechosAgregados() {
        return hechosAgregados;
    }

    public void setHechosAgregados(Integer hechosAgregados) {
        this.hechosAgregados = hechosAgregados;
    }

    public List<String> getErrores() {
        return errores;
    }

    public void setErrores(List<String> errores) {
        this.errores = errores;
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }
}

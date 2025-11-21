package utn.dds.agregador.dominio;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agregacion_job")
public class AgregacionJob {

    @Id
    @Column(name = "id", length = 255)
    private String id;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 50)
    private EstadoJob estado;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    // Progreso
    @Column(name = "fuentes_consultadas")
    private Integer fuentesConsultadas = 0;

    @Column(name = "total_fuentes")
    private Integer totalFuentes = 0;

    @Column(name = "hechos_obtenidos")
    private Integer hechosObtenidos = 0;

    @Column(name = "hechos_agregados")
    private Integer hechosAgregados = 0;

    // Errores como texto separado por saltos de línea
    @Column(name = "errores", columnDefinition = "TEXT")
    private String errores;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Constructores
    public AgregacionJob() {
    }

    public AgregacionJob(String id, Integer totalFuentes) {
        this.id = id;
        this.estado = EstadoJob.EN_PROGRESO;
        this.fechaInicio = LocalDateTime.now();
        this.totalFuentes = totalFuentes;
        this.fuentesConsultadas = 0;
        this.hechosObtenidos = 0;
        this.hechosAgregados = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    // Métodos de actualización
    public void actualizarProgreso(int fuentesConsultadas, int hechosObtenidos) {
        this.fuentesConsultadas = fuentesConsultadas;
        this.hechosObtenidos = hechosObtenidos;
        this.updatedAt = LocalDateTime.now();
    }

    public void completar(int hechosAgregados, List<String> erroresList) {
        this.estado = EstadoJob.COMPLETADO;
        this.fechaFin = LocalDateTime.now();
        this.hechosAgregados = hechosAgregados;
        if (erroresList != null && !erroresList.isEmpty()) {
            this.errores = String.join("\n", erroresList);
        }
        this.updatedAt = LocalDateTime.now();
    }

    public void marcarComoError(String mensajeError) {
        this.estado = EstadoJob.ERROR;
        this.fechaFin = LocalDateTime.now();
        this.mensajeError = mensajeError;
        this.updatedAt = LocalDateTime.now();
    }

    // Getters y Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public EstadoJob getEstado() {
        return estado;
    }

    public void setEstado(EstadoJob estado) {
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

    public String getErrores() {
        return errores;
    }

    public void setErrores(String errores) {
        this.errores = errores;
    }

    public List<String> getErroresAsList() {
        if (errores == null || errores.isEmpty()) {
            return new ArrayList<>();
        }
        return List.of(errores.split("\n"));
    }

    public String getMensajeError() {
        return mensajeError;
    }

    public void setMensajeError(String mensajeError) {
        this.mensajeError = mensajeError;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

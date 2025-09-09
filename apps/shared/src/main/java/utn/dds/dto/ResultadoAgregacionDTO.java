package utn.dds.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ResultadoAgregacionDTO {
    private int fuentesConsultadas;
    private int hechosObtenidos;
    private int hechosAlmacenados;
    private LocalDateTime fechaEjecucion;
    private List<String> fuentesConError;
    
    public ResultadoAgregacionDTO() {}
    
    public ResultadoAgregacionDTO(int fuentesConsultadas, int hechosObtenidos, int hechosAlmacenados, LocalDateTime fechaEjecucion, List<String> fuentesConError) {
        this.fuentesConsultadas = fuentesConsultadas;
        this.hechosObtenidos = hechosObtenidos;
        this.hechosAlmacenados = hechosAlmacenados;
        this.fechaEjecucion = fechaEjecucion;
        this.fuentesConError = fuentesConError;
    }
    
    public int getFuentesConsultadas() {
        return fuentesConsultadas;
    }
    
    public void setFuentesConsultadas(int fuentesConsultadas) {
        this.fuentesConsultadas = fuentesConsultadas;
    }
    
    public int getHechosObtenidos() {
        return hechosObtenidos;
    }
    
    public void setHechosObtenidos(int hechosObtenidos) {
        this.hechosObtenidos = hechosObtenidos;
    }
    
    public int getHechosAlmacenados() {
        return hechosAlmacenados;
    }
    
    public void setHechosAlmacenados(int hechosAlmacenados) {
        this.hechosAlmacenados = hechosAlmacenados;
    }
    
    public LocalDateTime getFechaEjecucion() {
        return fechaEjecucion;
    }
    
    public void setFechaEjecucion(LocalDateTime fechaEjecucion) {
        this.fechaEjecucion = fechaEjecucion;
    }
    
    public List<String> getFuentesConError() {
        return fuentesConError;
    }
    
    public void setFuentesConError(List<String> fuentesConError) {
        this.fuentesConError = fuentesConError;
    }
}
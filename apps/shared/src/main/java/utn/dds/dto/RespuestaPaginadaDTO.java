package utn.dds.dto;

import java.util.List;

/**
 * DTO genérico para respuestas paginadas
 */
public class RespuestaPaginadaDTO<T> {
    private List<T> datos;
    private int pagina;
    private int tamanioPagina;
    private long totalElementos;
    private int totalPaginas;
    private boolean tieneSiguiente;
    private boolean tieneAnterior;

    public RespuestaPaginadaDTO() {}

    public RespuestaPaginadaDTO(List<T> datos, int pagina, int tamanioPagina, long totalElementos) {
        this.datos = datos;
        this.pagina = pagina;
        this.tamanioPagina = tamanioPagina;
        this.totalElementos = totalElementos;
        this.totalPaginas = (int) Math.ceil((double) totalElementos / tamanioPagina);
        this.tieneSiguiente = pagina < totalPaginas - 1;
        this.tieneAnterior = pagina > 0;
    }

    public List<T> getDatos() {
        return datos;
    }

    public void setDatos(List<T> datos) {
        this.datos = datos;
    }

    public int getPagina() {
        return pagina;
    }

    public void setPagina(int pagina) {
        this.pagina = pagina;
    }

    public int getTamanioPagina() {
        return tamanioPagina;
    }

    public void setTamanioPagina(int tamanioPagina) {
        this.tamanioPagina = tamanioPagina;
    }

    public long getTotalElementos() {
        return totalElementos;
    }

    public void setTotalElementos(long totalElementos) {
        this.totalElementos = totalElementos;
    }

    public int getTotalPaginas() {
        return totalPaginas;
    }

    public void setTotalPaginas(int totalPaginas) {
        this.totalPaginas = totalPaginas;
    }

    public boolean isTieneSiguiente() {
        return tieneSiguiente;
    }

    public void setTieneSiguiente(boolean tieneSiguiente) {
        this.tieneSiguiente = tieneSiguiente;
    }

    public boolean isTieneAnterior() {
        return tieneAnterior;
    }

    public void setTieneAnterior(boolean tieneAnterior) {
        this.tieneAnterior = tieneAnterior;
    }
}
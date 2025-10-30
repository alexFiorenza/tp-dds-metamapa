package utn.dds.dto;

import utn.dds.dominio.criterios.*;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class CriterioCreateDTO {
    private String tipo;

    // Campos para diferentes tipos de criterios - solo se usa el campo correspondiente al tipo
    private String titulo;
    private String categoria;
    private String descripcion;
    private String origen;
    private LocalDate fechaAcontecimiento;
    private LocalDateTime fechaCarga;
    private Double longitud;
    private Double latitud;
    private String estado;
    private String etiquetas; // Comma-separated values para múltiples etiquetas

    public CriterioCreateDTO() {}

    // Método para convertir DTO a HechoStrategy
    public HechoStrategy toHechoStrategy() {
        switch (tipo.toLowerCase()) {
            case "titulo":
                return new TituloStrategy(titulo);
            case "categoria":
                return new CategoriaStrategy(categoria);
            case "descripcion":
                return new DescripcionStrategy(descripcion);
            case "origen":
                return new OrigenStrategy(origen);
            case "fechaacontecimiento":
                return new FechaAcontecimientoStrategy(fechaAcontecimiento);
            case "fechacarga":
                return new FechaCargaStrategy(fechaCarga);
            case "longitud":
                return new LongitudStrategy(longitud);
            case "latitud":
                return new LatitudStrategy(latitud);
            case "estado":
                EstadoHecho estadoEnum = EstadoHecho.valueOf(estado.toUpperCase());
                return new EstadoStrategy(estadoEnum);
            case "etiquetas":
                List<String> etiquetasList = Arrays.asList(etiquetas.split(","));
                return new EtiquetasStrategy(etiquetasList);
            default:
                throw new IllegalArgumentException("Tipo de criterio no válido: " + tipo);
        }
    }

    // Getters y Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public LocalDate getFechaAcontecimiento() {
        return fechaAcontecimiento;
    }

    public void setFechaAcontecimiento(LocalDate fechaAcontecimiento) {
        this.fechaAcontecimiento = fechaAcontecimiento;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(String etiquetas) {
        this.etiquetas = etiquetas;
    }
}
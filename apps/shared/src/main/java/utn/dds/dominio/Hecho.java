package utn.dds.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Hecho {
    private String uuid;
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDate fechaAcontecimiento;
    private String origen;
    private Contribuyente contribuyente;
    private TipoHecho tipo;
    private double longitud;
    private double latitud;
    private LocalDateTime fechaCarga;
    private EstadoHecho estado;
    private List<String> etiquetas;

    public Hecho() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Constructor
    public Hecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                 String origen, Contribuyente contribuyente, TipoHecho tipo,
                 double longitud, double latitud, LocalDateTime fechaCarga,
                 EstadoHecho estado, List<String> etiquetas) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fechaAcontecimiento = fechaAcontecimiento;
        this.origen = origen;
        this.contribuyente = contribuyente;
        this.tipo = tipo;
        this.longitud = longitud;
        this.latitud = latitud;
        this.fechaCarga = fechaCarga;
        this.estado = estado;
        this.etiquetas = etiquetas;
    }

    public Hecho(Map<String, Object> datos) {
        this.uuid = (String) datos.getOrDefault("uuid", UUID.randomUUID().toString());
        this.titulo = (String) datos.get("titulo");
        this.descripcion = (String) datos.get("descripcion");
        this.categoria = (String) datos.get("categoria");
        this.fechaAcontecimiento = (LocalDate) datos.get("fecha_contecimiento");
        this.origen = (String) datos.get("origen");
        this.contribuyente = (Contribuyente) datos.get("contribuyente");
        Object tipoObj = datos.get("tipo");
        this.tipo = tipoObj instanceof String ? 
            TipoHecho.valueOf(((String) tipoObj).toUpperCase()) : 
            (TipoHecho) tipoObj;
        this.longitud = (Double) datos.get("longitud");
        this.latitud = (Double) datos.get("latitud");
        this.fechaCarga = (LocalDateTime) datos.get("fecha_carga");
        this.estado = EstadoHecho.ACTIVO;
        this.etiquetas = (List<String>) datos.get("etiquetas");
    }

    // Getters
    public String getTitulo() {
        return titulo;
    }

    public String getUuid() {
        return uuid;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public LocalDate getFechaAcontecimiento() {
        return fechaAcontecimiento;
    }

    public String getOrigen() {
        return origen;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public TipoHecho getTipo() {
        return tipo;
    }

    public double getLongitud() {
        return longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public EstadoHecho getEstado() {
        return estado;
    }

    public List<String> getEtiquetas() {
        return etiquetas;
    }

    // Setters
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setFechaAcontecimiento(LocalDate fechaAcontecimiento) {
        this.fechaAcontecimiento = fechaAcontecimiento;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public void setTipo(TipoHecho tipo) {
        this.tipo = tipo;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public void setEstado(EstadoHecho estado) {
        this.estado = estado;
    }

    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas;
    }

    // Acciones
    public void ocultar() {
        this.estado = EstadoHecho.OCULTO;
    }

    public void activar() {
        this.estado = EstadoHecho.ACTIVO;
    }
} 
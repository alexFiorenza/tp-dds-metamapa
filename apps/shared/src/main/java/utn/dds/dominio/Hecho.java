package utn.dds.dominio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Hecho {
    private final String titulo;
    private final String descripcion;
    private final String categoria;
    private final LocalDate fechaAcontecimiento;
    private final Origen origen;
    private final Contribuyente contribuyente;
    private final TipoHecho tipo;
    private final double longitud;
    private final double latitud;
    private final LocalDateTime fechaCarga;
    private EstadoHecho estado;
    private final List<String> etiquetas;
    private final String uuid; // Esto es para identificar en la base de datos o en memoria en caso de Estatica

    // Constructor
    public Hecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                 Origen origen, Contribuyente contribuyente, TipoHecho tipo,
                 double longitud, double latitud, LocalDateTime fechaCarga,
                 EstadoHecho estado, List<String> etiquetas) {
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
        this.uuid = UUID.randomUUID().toString();
    }

    public Hecho(Map<String, Object> datos) {
        this.titulo = (String) datos.get("titulo");
        this.descripcion = (String) datos.get("descripcion");
        this.categoria = (String) datos.get("categoria");
        this.fechaAcontecimiento = (LocalDate) datos.get("fecha_contecimiento");
        this.origen = Origen.MANUAL;
        this.contribuyente = (Contribuyente) datos.get("contribuyente");
        this.tipo = (TipoHecho) datos.get("tipo");
        this.longitud = (Double) datos.get("longitud");
        this.latitud = (Double) datos.get("latitud");
        this.fechaCarga = (LocalDateTime) datos.get("fecha_carga");
        this.estado = EstadoHecho.ACTIVO;
        this.etiquetas = (List<String>) datos.get("etiquetas");
        this.uuid = (String) datos.get("uuid");
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

    public Origen getOrigen() {
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

    // Acciones
    public void ocultar() {
        this.estado = EstadoHecho.OCULTO;
    }

    public void activar() {
        this.estado = EstadoHecho.ACTIVO;
    }
} 
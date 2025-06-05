package utn.dds.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Hecho {
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDate fechaAcontecimiento;
    private Origen origen;
    private Contribuyente contribuyente;
    private TipoHecho tipo;
    private double longitud;
    private double latitud;
    private LocalDate fechaCarga;
    private EstadoHecho estado;
    private List<String> etiquetas;

    // Constructor
    public Hecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                 Origen origen, Contribuyente contribuyente, TipoHecho tipo,
                 double longitud, double latitud, LocalDate fechaCarga,
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
    }


    // Getters
    public String getTitulo() {
        return titulo;
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

    public LocalDate getFechaCarga() {
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
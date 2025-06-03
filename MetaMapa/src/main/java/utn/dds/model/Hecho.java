package utn.dds.model;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Hecho {
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDate fechaAcontecimiento;
    private Origen origen;    // hay que crear la clase Origen
    private Contribuyente contribuyente;  // hay que crear la clase Contribuyente
    private TipoHecho tipo; // hay que crear la clase TipoHecho
    private Int longitud;
    private Int latitud;
    private LocalDate fechaCarga;
    private EstadoHecho estado;  // hay que crear la clase EstadoHecho
    private List<String> etiquetas;

    // Constructor
    public Hecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                 Origen origen, Contribuyente contribuyente, TipoHecho tipo,
                 Integer longitud, Integer latitud, LocalDate fechaCarga,
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

    public void ocultar(){

    }

    public void activar(){

    }
}
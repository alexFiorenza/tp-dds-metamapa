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

    // No se si habria que implementar un constructor
    public Hecho(){

    }

    public void ocultar(){

    }

    public void activar(){

    }
}

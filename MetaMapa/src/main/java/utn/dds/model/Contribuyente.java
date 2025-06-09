package utn.dds.model;
import utn.dds.service.negocio.ContribuyenteService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Contribuyente {
    private List<Hecho> aportes;
    private String nombre;
    private String apellido;
    private int edad;

    // Constructor
    public Contribuyente(String nombre, String apellido, Integer edad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.aportes = new ArrayList<>();
    }

    // Invoco al servicio
    ContribuyenteService servicio = new ContribuyenteService();

    public Hecho crearHecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                           Origen origen, TipoHecho tipo,
                           double longitud, double latitud, LocalDate fechaCarga,
                           EstadoHecho estado, List<String> etiquetas){

        Hecho nuevoHecho = servicio.crearHecho(this, titulo, descripcion, categoria, fechaAcontecimiento, origen, tipo,
                                    longitud, latitud, fechaCarga, estado, etiquetas);

        // Aca habria que poner un if, de si fue aceptado el hecho o fue rechazado
        // Si fue aceptado, habria que agregarlo a la lista de aportes
        /*
        if(fueAceptado()){
            aportes.add(nuevoHecho)
        }
         */

        return nuevoHecho;
    }

    // Getters
    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public int getEdad() {
        return edad;
    }

    public List<Hecho> getAportes() {
        return aportes;
    }


}

package utn.dds.model;
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

    // No se si se deberian pasar las fuentes por atributo, y tampoco si esto seria repeticion de codigo por lo de Hecho
    public void crearHecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                           Origen origen, TipoHecho tipo,
                           double longitud, double latitud, LocalDate fechaCarga,
                           EstadoHecho estado, List<String> etiquetas, String uuid){
        // No sabria si tengo que poner asi el atributo uuid

        Contribuyente info = new Contribuyente(this.nombre, this.apellido, this.edad);

        Hecho nuevoHecho = new Hecho(titulo, descripcion, categoria, fechaAcontecimiento,
                                    origen, info, tipo, longitud, latitud, fechaCarga,
                                    estado, etiquetas, uuid);

        // aca no se si añadirlo directamente a la lista de aportes, o si tengo que hacer otrafuncion para
        // saber si fue aceptada la subida del hecho o no
        aportes.add(nuevoHecho);
    }

}

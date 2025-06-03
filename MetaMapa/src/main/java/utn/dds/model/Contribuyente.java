package utn.dds.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Contribuyente {
    private List<Hecho> aportes;
    private String nombre;
    private Optional <String> apellido;    // Creo que asi se ponen opcionalmentre los atributos
    private String edad;   // No seria mejor usar un INT aca ?

    public Contribuyente(String nombre, Optional<String> apellido, Integer edad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.aportes = new ArrayList<>();
    }

    public subirHecho(){

    }
}

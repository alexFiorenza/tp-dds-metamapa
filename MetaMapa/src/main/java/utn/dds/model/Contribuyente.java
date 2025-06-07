package utn.dds.model;
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
}

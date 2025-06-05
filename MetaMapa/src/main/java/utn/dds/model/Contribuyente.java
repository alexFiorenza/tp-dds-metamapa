package utn.dds.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class Contribuyente {
    private List<Hecho> aportes;
    private String nombre;
    private Optional <String> apellido;
    private int edad;

    // Constructor
    public Contribuyente(String nombre, Optional<String> apellido, Integer edad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.aportes = new ArrayList<>();
    }

    // Para hacer necesito la base de datos a donde va a subir el hecho
    public void subirHecho(){

    }
}

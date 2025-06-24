package utn.dds.model;

import java.util.ArrayList;
import java.util.List;

public class Contribuyente {
    private final List<Hecho> aportes;
    private final String nombre;
    private final String apellido;
    private final int edad;

    // Constructor
    public Contribuyente(String nombre, String apellido, Integer edad) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.aportes = new ArrayList<>();
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
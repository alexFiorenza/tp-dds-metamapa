package utn.dds.dominio;

import jakarta.persistence.*;

@Entity
@Table(name = "contribuyentes")
public class Contribuyente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre", nullable = false)
    private String nombre;

    @Column(name = "apellido", nullable = false)
    private String apellido;

    @Column(name = "edad")
    private Integer edad;

    @Column(name = "user_id", unique = true, length = 255)
    private String userId;

    public Contribuyente() {
    }

    // Constructor
    public Contribuyente(String nombre, String apellido, Integer edad) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad; // Puede ser null
    }

    // Constructor con User ID
    public Contribuyente(String nombre, String apellido, Integer edad, String userId) {
        this();
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad; // Puede ser null
        this.userId = userId;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public Integer getEdad() {
        return edad;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
} 
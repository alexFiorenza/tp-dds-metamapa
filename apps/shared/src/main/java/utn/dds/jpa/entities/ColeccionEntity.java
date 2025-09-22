package utn.dds.jpa.entities;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "colecciones")
public class ColeccionEntity {

    @Id
    @Column(name = "handle")
    private String handle;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "descripcion")
    private String descripcion;

    @ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JoinTable(
        name = "coleccion_hechos",
        joinColumns = @JoinColumn(name = "coleccion_handle"),
        inverseJoinColumns = @JoinColumn(name = "hecho_uuid")
    )
    private List<HechoEntity> hechos;

    public ColeccionEntity() {
        this.handle = UUID.randomUUID().toString();
        this.hechos = new ArrayList<>();
    }

    // Getters
    public String getHandle() {
        return handle;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<HechoEntity> getHechos() {
        return hechos;
    }

    // Setters
    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setHechos(List<HechoEntity> hechos) {
        this.hechos = hechos;
    }
}
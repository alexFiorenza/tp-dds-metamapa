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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "coleccion_hechos",
        joinColumns = @JoinColumn(name = "coleccion_handle"),
        inverseJoinColumns = @JoinColumn(name = "hecho_uuid")
    )
    private List<HechoEntity> hechos;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "coleccion_fuentes",
        joinColumns = @JoinColumn(name = "coleccion_handle"),
        inverseJoinColumns = @JoinColumn(name = "fuente_uuid")
    )
    private List<FuenteEntity> fuentes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "coleccion_handle")
    private List<CriterioEntity> criteriosDePertenencia;

    public ColeccionEntity() {
        this.handle = UUID.randomUUID().toString();
        this.hechos = new ArrayList<>();
        this.fuentes = new ArrayList<>();
        this.criteriosDePertenencia = new ArrayList<>();
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

    public List<FuenteEntity> getFuentes() {
        return fuentes;
    }

    public List<CriterioEntity> getCriteriosDePertenencia() {
        return criteriosDePertenencia;
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

    public void setFuentes(List<FuenteEntity> fuentes) {
        this.fuentes = fuentes;
    }

    public void setCriteriosDePertenencia(List<CriterioEntity> criteriosDePertenencia) {
        this.criteriosDePertenencia = criteriosDePertenencia;
    }
}
package utn.dds.dominio;

import jakarta.persistence.*;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dominio.consenso.AlgoritmoConsenso;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "colecciones")
public class Coleccion {

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
    private List<Hecho> hechos;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "coleccion_fuentes",
        joinColumns = @JoinColumn(name = "coleccion_handle"),
        inverseJoinColumns = @JoinColumn(name = "fuente_uuid")
    )
    private List<Fuente> fuentes;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @JoinColumn(name = "coleccion_handle")
    private List<Criterio> criteriosDePertenencia;

    @Convert(converter = utn.dds.dominio.consenso.AlgoritmoConsensoConverter.class)
    @Column(name = "algoritmo_consenso")
    private AlgoritmoConsenso algoritmoConsenso;

    public Coleccion() {
        this.handle = UUID.randomUUID().toString();
        this.hechos = new ArrayList<>();
        this.fuentes = new ArrayList<>();
        this.criteriosDePertenencia = new ArrayList<>();
        this.algoritmoConsenso = new utn.dds.dominio.consenso.ConsensoDefault(); // Por defecto, todos los hechos son consensuados
    }

    // Constructor
    public Coleccion(String titulo, String descripcion,
                     List<Hecho> hechos, List<Criterio> criteriosDePertenencia) {
        this();
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.hechos = hechos;
        this.criteriosDePertenencia = criteriosDePertenencia;
    }

    public List<Hecho> buscarHechos(List<HechoStrategy> filtros) {
        List<Hecho> resultados = new ArrayList<>();

        for (Hecho hecho : hechos) {
            boolean cumpleTodos = true;
            for (HechoStrategy filtro : filtros) {
                if (!filtro.cumple(hecho)) {
                    cumpleTodos = false;
                    break;
                }
            }
            if (cumpleTodos) {
                resultados.add(hecho);
            }
        }

        return resultados;
    }

    // Método auxiliar para obtener criterios como HechoStrategy
    public List<HechoStrategy> getCriteriosDePertenenciaAsStrategies() {
        if (criteriosDePertenencia == null) {
            return new ArrayList<>();
        }
        return criteriosDePertenencia.stream()
                .map(Criterio::toHechoStrategy)
                .collect(Collectors.toList());
    }

    // Getters
    public String getHandle() {
        return handle;
    }

    public String getTitulo(){
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<Criterio> getCriteriosDePertenencia(){
        return criteriosDePertenencia;
    }

    public List<Hecho> getHechos() {
        return hechos;
    }

    public List<Fuente> getFuentes() {
        return fuentes;
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

    public void setHechos(List<Hecho> hechos){
        this.hechos = hechos;
    }

    public void setFuentes(List<Fuente> fuentes) {
        this.fuentes = fuentes;
    }

    public void setCriteriosDePertenencia(List<Criterio> criteriosDePertenencia) {
        this.criteriosDePertenencia = criteriosDePertenencia;
    }

    public AlgoritmoConsenso getAlgoritmoConsenso() {
        return algoritmoConsenso;
    }

    public void setAlgoritmoConsenso(AlgoritmoConsenso algoritmoConsenso) {
        this.algoritmoConsenso = algoritmoConsenso != null ? algoritmoConsenso : new utn.dds.dominio.consenso.ConsensoDefault();
    }
} 
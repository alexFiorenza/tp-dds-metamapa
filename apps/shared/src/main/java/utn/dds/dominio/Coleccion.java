package utn.dds.dominio;

import utn.dds.dominio.criterios.HechoStrategy;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Coleccion {

    private String handle;
    private String titulo;
    private String descripcion;
    private List<Hecho> hechos;
    private List<HechoStrategy> criteriosDePertenencia;

    public Coleccion() {
        this.handle = UUID.randomUUID().toString();
        this.hechos = new ArrayList<>();
        this.criteriosDePertenencia = new ArrayList<>();
    }

    // Constructor
    public Coleccion(String titulo, String descripcion,
                     List<Hecho> hechos, List<HechoStrategy> criteriosDePertenencia) {
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

    public List<HechoStrategy> getCriteriosDePertenencia(){
        return criteriosDePertenencia;
    }

    public List<Hecho> getHechos() {
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

    public void setHechos(List<Hecho> hechos){
        this.hechos = hechos;
    }

    public void setCriteriosDePertenencia(List<HechoStrategy> criteriosDePertenencia) {
        this.criteriosDePertenencia = criteriosDePertenencia;
    }
} 
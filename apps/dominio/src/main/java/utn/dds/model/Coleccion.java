package utn.dds.model;

import utn.dds.model.criterios.HechoStrategy;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

public class Coleccion {

    private final String titulo;
    private String descripcion;
    private List<Hecho> hechos;
    private List<HechoStrategy> criteriosDePertenencia;
    private String handle;

    // Constructor
    public Coleccion(String titulo, String descripcion,
                     List<Hecho> hechos, List<HechoStrategy> criteriosDePertenencia) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.hechos = hechos;
        this.criteriosDePertenencia = criteriosDePertenencia;
        this.handle = UUID.randomUUID().toString();
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
    public String getTitulo(){
        return titulo;
    }

    public List<HechoStrategy> getCriteriosDePertenencia(){
        return criteriosDePertenencia;
    }

    public List<Hecho> getHechos() {
        return hechos;
    }

    // Setters
    public void setHechos(List<Hecho> hechos){
        this.hechos = hechos;
    }
} 
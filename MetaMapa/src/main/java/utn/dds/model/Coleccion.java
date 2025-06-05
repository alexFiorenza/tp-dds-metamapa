package utn.dds.model;

import java.util.List;
import java.util.ArrayList;

public class Coleccion {

    private String titulo;
    private String descripcion;
    private FuenteDeDatos fuente;   // Hay que crear esta clase
    private List<Hecho> hechos;
    private List<HechoStrategy> criteriosDePertenencia;   // Hay que crear esta clase
    private String handle;

    // Constructor
    public Coleccion(String titulo, String descripcion, FuenteDeDatos fuente,
                     List<Hecho> hechos, List<HechoStrategy> criteriosDePertenencia, String handle) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fuente = fuente;
        this.hechos = hechos;
        this.criteriosDePertenencia = criteriosDePertenencia;
        this.handle = handle;
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
}

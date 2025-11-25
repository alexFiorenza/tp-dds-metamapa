package utn.dds.dominio.consenso;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.Fuente;
import java.util.List;

/**
 * Interfaz que define el contrato para los algoritmos de consenso.
 * Cada implementación determina qué hechos están consensuados según diferentes criterios.
 */
public interface AlgoritmoConsenso {
    /**
     * Filtra una lista de hechos, retornando solo aquellos que cumplen con el criterio de consenso.
     * Los hechos se agrupan por título y se evalúan en grupos.
     * 
     * @param hechos Lista completa de hechos a filtrar
     * @param fuentes Lista de fuentes de la colección
     * @return Lista de hechos que cumplen con el criterio de consenso
     */
    List<Hecho> filtrarHechosConsensuados(List<Hecho> hechos, List<Fuente> fuentes);
}


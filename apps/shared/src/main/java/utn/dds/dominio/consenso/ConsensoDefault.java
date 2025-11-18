package utn.dds.dominio.consenso;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.Fuente;
import java.util.List;
import java.util.ArrayList;

/**
 * Implementación por defecto del algoritmo de consenso.
 * Todos los hechos se consideran consensuados y estarán disponibles
 * en la navegación curada.
 */
public class ConsensoDefault implements AlgoritmoConsenso {
    @Override
    public List<Hecho> filtrarHechosConsensuados(List<Hecho> hechos, List<Fuente> fuentes) {
        // Por defecto, todos los hechos se consideran consensuados
        return new ArrayList<>(hechos);
    }
}


package utn.dds.dominio.consenso;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.Fuente;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del algoritmo de múltiples menciones.
 * Si al menos dos fuentes contienen un mismo hecho y ninguna otra fuente
 * contiene otro de igual título pero diferentes atributos, se lo considera consensuado.
 */
public class ConsensoMenciones implements AlgoritmoConsenso {
    @Override
    public List<Hecho> filtrarHechosConsensuados(List<Hecho> hechos, List<Fuente> fuentes) {
        if (hechos == null || hechos.isEmpty() || fuentes == null || fuentes.isEmpty()) {
            return new ArrayList<>();
        }

        List<Hecho> hechosConsensuados = new ArrayList<>();
        
        // Agrupar hechos por título
        Map<String, List<Hecho>> hechosPorTitulo = hechos.stream()
            .collect(Collectors.groupingBy(Hecho::getTitulo));

        for (Map.Entry<String, List<Hecho>> entry : hechosPorTitulo.entrySet()) {
            List<Hecho> hechosConMismoTitulo = entry.getValue();

            // Contar cuántas fuentes diferentes tienen hechos con este título
            long fuentesConEsteTitulo = hechosConMismoTitulo.stream()
                .map(Hecho::getOrigen)
                .distinct()
                .count();

            // Verificar que al menos 2 fuentes tienen este hecho
            if (fuentesConEsteTitulo >= 2) {
                // Verificar que no haya conflictos
                if (!tieneConflictos(hechosConMismoTitulo)) {
                    hechosConsensuados.addAll(hechosConMismoTitulo);
                }
            }
        }

        return hechosConsensuados;
    }

    /**
     * Verifica si hay conflictos entre hechos con el mismo título.
     */
    private boolean tieneConflictos(List<Hecho> hechos) {
        if (hechos.size() <= 1) {
            return false;
        }

        Hecho referencia = hechos.get(0);
        for (int i = 1; i < hechos.size(); i++) {
            if (!sonSimilares(referencia, hechos.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean sonSimilares(Hecho h1, Hecho h2) {
        if (!h1.getTitulo().equals(h2.getTitulo())) {
            return false;
        }

        // Comparar descripción
        if (h1.getDescripcion() != null && h2.getDescripcion() != null) {
            if (!h1.getDescripcion().trim().equalsIgnoreCase(h2.getDescripcion().trim())) {
                return false;
            }
        } else if (h1.getDescripcion() != null || h2.getDescripcion() != null) {
            return false;
        }

        // Comparar categoría
        if (h1.getCategoria() != null && h2.getCategoria() != null) {
            if (!h1.getCategoria().equals(h2.getCategoria())) {
                return false;
            }
        }

        // Comparar coordenadas (tolerancia de 0.001 grados)
        double tolerancia = 0.001;
        if (Math.abs(h1.getLatitud() - h2.getLatitud()) > tolerancia ||
            Math.abs(h1.getLongitud() - h2.getLongitud()) > tolerancia) {
            return false;
        }

        return true;
    }
}


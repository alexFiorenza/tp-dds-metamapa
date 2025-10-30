package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;
import java.util.List;

public class EtiquetasStrategy implements HechoStrategy {
    private final List<String> etiquetas;

    public EtiquetasStrategy(List<String> etiquetas) {
        this.etiquetas = etiquetas;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        if (hecho.getEtiquetas() == null || hecho.getEtiquetas().isEmpty()) {
            return false;
        }

        // Verifica que el hecho contenga al menos una de las etiquetas buscadas
        return hecho.getEtiquetas().stream()
                .anyMatch(etiquetaHecho -> etiquetas.stream()
                        .anyMatch(etiquetaBuscada -> etiquetaHecho.toLowerCase().contains(etiquetaBuscada.toLowerCase())));
    }

    public List<String> getEtiquetas() {
        return etiquetas;
    }
}
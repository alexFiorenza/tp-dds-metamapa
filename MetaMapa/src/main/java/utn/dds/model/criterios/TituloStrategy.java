package utn.dds.model.criterios;

import utn.dds.model.Hecho;

public class TituloStrategy implements HechoStrategy {
    private final String titulo;

    public TituloStrategy(String titulo) {
        this.titulo = titulo;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return hecho.getTitulo().toLowerCase().contains(titulo);
    }
}

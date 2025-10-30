package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;

public class DescripcionStrategy implements HechoStrategy {
    private final String descripcion;

    public DescripcionStrategy(String descripcion) {
        this.descripcion = descripcion.toLowerCase();
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return hecho.getDescripcion().toLowerCase().contains(descripcion);
    }

    public String getDescripcion() {
        return descripcion;
    }
}
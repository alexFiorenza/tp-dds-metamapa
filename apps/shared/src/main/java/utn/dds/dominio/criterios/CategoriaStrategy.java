package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;

public class CategoriaStrategy implements HechoStrategy{
    private final String categoria;

    public CategoriaStrategy(String categoria) {
        this.categoria = categoria;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return hecho.getCategoria().toLowerCase().contains(categoria);
    }
} 
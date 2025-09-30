package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;

public class OrigenStrategy implements HechoStrategy {
    private final String origen;

    public OrigenStrategy(String origen) {
        this.origen = origen;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return hecho.getOrigen().equals(origen);
    }

    public String getOrigen() {
        return origen;
    }
}
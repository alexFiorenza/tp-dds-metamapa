package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;

public class LatitudStrategy implements HechoStrategy {
    private final double latitud;

    public LatitudStrategy(double latitud) {
        this.latitud = latitud;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return Double.compare(hecho.getLatitud(), latitud) == 0;
    }

    public double getLatitud() {
        return latitud;
    }
}
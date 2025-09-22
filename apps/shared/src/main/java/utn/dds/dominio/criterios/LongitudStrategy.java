package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;

public class LongitudStrategy implements HechoStrategy {
    private final double longitud;

    public LongitudStrategy(double longitud) {
        this.longitud = longitud;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return Double.compare(hecho.getLongitud(), longitud) == 0;
    }

    public double getLongitud() {
        return longitud;
    }
}
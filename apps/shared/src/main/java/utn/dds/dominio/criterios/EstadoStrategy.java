package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.EstadoHecho;

public class EstadoStrategy implements HechoStrategy {
    private final EstadoHecho estado;

    public EstadoStrategy(EstadoHecho estado) {
        this.estado = estado;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return hecho.getEstado().equals(estado);
    }

    public EstadoHecho getEstado() {
        return estado;
    }
}
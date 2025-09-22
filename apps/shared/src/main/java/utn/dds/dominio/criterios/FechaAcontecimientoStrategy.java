package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;
import java.time.LocalDate;

public class FechaAcontecimientoStrategy implements HechoStrategy {
    private final LocalDate fechaAcontecimiento;

    public FechaAcontecimientoStrategy(LocalDate fechaAcontecimiento) {
        this.fechaAcontecimiento = fechaAcontecimiento;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return hecho.getFechaAcontecimiento().equals(fechaAcontecimiento);
    }

    public LocalDate getFechaAcontecimiento() {
        return fechaAcontecimiento;
    }
}
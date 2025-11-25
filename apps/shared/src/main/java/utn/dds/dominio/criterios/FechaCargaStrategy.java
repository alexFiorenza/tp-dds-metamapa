package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;
import java.time.LocalDateTime;

public class FechaCargaStrategy implements HechoStrategy {
    private final LocalDateTime fechaCarga;

    public FechaCargaStrategy(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    @Override
    public boolean cumple(Hecho hecho) {
        return hecho.getFechaCarga().equals(fechaCarga);
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }
}
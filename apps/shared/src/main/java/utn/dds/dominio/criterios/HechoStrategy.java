package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;

public interface HechoStrategy {
    boolean cumple(Hecho hecho);
} 
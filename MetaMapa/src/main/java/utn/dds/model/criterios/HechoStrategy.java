package utn.dds.model.criterios;

import utn.dds.model.Hecho;

public interface HechoStrategy {
    boolean cumple(Hecho hecho);
}



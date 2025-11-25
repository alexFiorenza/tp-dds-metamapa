package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;

public interface EstrategiaDeteccionDuplicados {
    boolean esDuplicado(Hecho hecho1, Hecho hecho2);
}
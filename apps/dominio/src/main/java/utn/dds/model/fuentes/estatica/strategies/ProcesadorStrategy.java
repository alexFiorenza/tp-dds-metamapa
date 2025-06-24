package utn.dds.model.fuentes.estatica.strategies;

import utn.dds.model.Hecho;

import java.util.List;

public interface ProcesadorStrategy {
    List<Hecho> obtenerHechos();
} 
package utn.dds.dominio.fuentes.estatica.strategies;

import utn.dds.dominio.Hecho;

public interface ProcesadorStrategy {
    Hecho procesar(String linea);
}

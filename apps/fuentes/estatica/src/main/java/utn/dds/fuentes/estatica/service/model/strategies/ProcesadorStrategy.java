package utn.dds.fuentes.estatica.service.model.strategies;

import utn.dds.dominio.Hecho;

public interface ProcesadorStrategy {
    Hecho procesar(String linea);
} 
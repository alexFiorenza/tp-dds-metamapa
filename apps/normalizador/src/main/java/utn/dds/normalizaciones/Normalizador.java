package utn.dds.normalizaciones;


import utn.dds.dto.HechoDTO;

public interface Normalizador {
    HechoDTO normalizar(HechoDTO hecho);
}

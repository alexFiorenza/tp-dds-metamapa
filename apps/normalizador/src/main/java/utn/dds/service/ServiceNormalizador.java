package utn.dds.service;

import utn.dds.dto.HechoDTO;
import utn.dds.normalizaciones.NormalizadorCategoria;


public class ServiceNormalizador {

    private static final NormalizadorCategoria normalizador = new NormalizadorCategoria();

    public static HechoDTO normalizar(HechoDTO hechoDTO) {
        return normalizador.normalizar(hechoDTO);
    }


}

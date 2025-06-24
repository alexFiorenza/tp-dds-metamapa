package utn.dds.dominio.fuentes;

import utn.dds.dominio.Hecho;

import java.util.List;

public interface FuenteDeDatos {
    List<Hecho> obtenerHechos();

    TipoFuente tipo();
} 
package utn.dds.model.fuentes;

import utn.dds.model.Hecho;
import utn.dds.model.TipoHecho;

import java.time.LocalDateTime;
import java.util.List;

public interface FuenteDeDatos {
    List<Hecho> obtenerHechos();

    TipoFuente tipo();
}

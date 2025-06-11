package utn.dds.model.fuentes;

import utn.dds.model.Hecho;
import java.util.List;

public class FuenteDinamica implements  FuenteDeDatos{
    private final TipoFuente tipoFuente;

    public FuenteDinamica() {
        this.tipoFuente = TipoFuente.DINAMICA;
    }


    @Override
    public List<Hecho> obtenerHechos() {
        return List.of();
    }

    @Override
    public TipoFuente tipo() {
        return tipoFuente;
    }
}

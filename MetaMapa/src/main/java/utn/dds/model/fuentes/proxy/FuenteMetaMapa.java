package utn.dds.model.fuentes.proxy;

import utn.dds.model.Hecho;
import utn.dds.model.fuentes.FuenteDeDatos;
import utn.dds.model.fuentes.TipoFuente;

import java.util.List;

public class FuenteMetaMapa implements FuenteDeDatos {
    private final TipoFuente tipoFuente;
    private final String handle;
    public FuenteMetaMapa(String handle) {
        this.handle = handle;
        this.tipoFuente = TipoFuente.PROXY;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        return List.of();
    }

    @Override
    public TipoFuente tipo() {
        return this.tipoFuente;
    }
}

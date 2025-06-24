package utn.dds.model.fuentes.estatica;

import utn.dds.model.Hecho;
import utn.dds.model.fuentes.FuenteDeDatos;
import utn.dds.model.fuentes.TipoFuente;
import utn.dds.model.fuentes.estatica.strategies.ProcesadorStrategy;

import java.util.List;

public class FuenteEstatica implements FuenteDeDatos {
    ProcesadorStrategy procesador;
    TipoFuente tipoFuente;

    public FuenteEstatica(ProcesadorStrategy procesador) {
        this.procesador = procesador;
        this.tipoFuente = TipoFuente.ESTATICA;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        return procesador.obtenerHechos();
    }

    @Override
    public TipoFuente tipo() {
        return tipoFuente;
    }
} 
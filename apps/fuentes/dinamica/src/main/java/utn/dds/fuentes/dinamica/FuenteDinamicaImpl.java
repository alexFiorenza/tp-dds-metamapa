package utn.dds.fuentes.dinamica;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;
import java.util.List;

public class FuenteDinamicaImpl implements FuenteDeDatos {
    private final TipoFuente tipoFuente;

    public FuenteDinamicaImpl() {
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
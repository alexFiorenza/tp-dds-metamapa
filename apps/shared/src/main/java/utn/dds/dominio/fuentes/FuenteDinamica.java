package utn.dds.dominio.fuentes;

import utn.dds.dominio.Hecho;
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
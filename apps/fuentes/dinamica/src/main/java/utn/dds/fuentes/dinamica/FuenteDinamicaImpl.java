package utn.dds.fuentes.dinamica;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;

import java.util.ArrayList;
import java.util.List;

public class FuenteDinamicaImpl implements FuenteDeDatos {
    private final TipoFuente tipoFuente;

    public FuenteDinamicaImpl() {
        this.tipoFuente = TipoFuente.DINAMICA;
    }

    // Aca no se que hacer

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<Hecho>();
        
        return hechos;
    }

    @Override
    public TipoFuente tipo() {
        return tipoFuente;
    }
} 
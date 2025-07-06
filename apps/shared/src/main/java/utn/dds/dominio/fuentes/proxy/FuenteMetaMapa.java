package utn.dds.dominio.fuentes.proxy;

import utn.dds.dominio.*;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;

import java.util.List;
import java.util.ArrayList;

public class FuenteMetaMapa implements FuenteDeDatos {
    private final TipoFuente tipoFuente;
    private String url;
    private String handle;
        
    public FuenteMetaMapa() {
        this.tipoFuente = TipoFuente.PROXY;
        this.handle = null;
        this.url = null;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        return new ArrayList<>();
    }

    @Override
    public TipoFuente tipo() {
        return this.tipoFuente;
    }
} 
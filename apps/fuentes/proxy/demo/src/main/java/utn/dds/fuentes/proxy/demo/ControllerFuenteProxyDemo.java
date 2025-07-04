package utn.dds.fuentes.proxy.demo;

import utn.dds.dominio.fuentes.proxy.FuenteProxyDemo;

public class ControllerFuenteProxyDemo {
    private final FuenteProxyDemo fuenteProxyDemo;
    private final int tiempo_cache;
    
    public ControllerFuenteProxyDemo(FuenteProxyDemo fuenteProxyDemo,int tiempo_cache) {
        this.fuenteProxyDemo = fuenteProxyDemo;
        this.tiempo_cache = tiempo_cache;
    }

    public String obtenerHechos() {
        throw new UnsupportedOperationException("No implementado");
    }
}

package utn.dds.dominio.fuentes.proxy;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public class FuenteProxyDemo implements FuenteDeDatos {
    private final Conexion conexion;
    private final URL url;
    private List<Hecho> cacheHechos;
    private LocalDateTime ultimaEjecucion;
    private final int TIEMPO_CACHE ;
    private final TipoFuente tipoFuente;

    public FuenteProxyDemo(Conexion conexion, URL url,int tiempo_cache) {
        this.conexion = conexion;
        this.url = url;
        this.cacheHechos = List.of();
        this.ultimaEjecucion = null;
        this.tipoFuente = TipoFuente.PROXY;
        this.TIEMPO_CACHE = tiempo_cache;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        if (ultimaEjecucion == null || ultimaEjecucion.plusMinutes(TIEMPO_CACHE).isBefore(LocalDateTime.now())) {
            // Actualizamos la caché
            FuenteDeDatos fuenteDemo = new FuenteDemo(this.conexion,this.url);
            this.cacheHechos = fuenteDemo.obtenerHechos();
            this.ultimaEjecucion = LocalDateTime.now();
        }

        return cacheHechos;
    }

    @Override
    public TipoFuente tipo() {
        return tipoFuente;
    }
} 
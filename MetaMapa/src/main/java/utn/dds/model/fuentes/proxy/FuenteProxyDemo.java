package utn.dds.model.fuentes.proxy;

import utn.dds.model.Hecho;
import utn.dds.model.fuentes.FuenteDeDatos;
import utn.dds.model.fuentes.TipoFuente;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;

public class FuenteProxyDemo implements FuenteDeDatos {
    private final Conexion conexion;
    private final URL url;
    private List<Hecho> cacheHechos;
    private LocalDateTime ultimaEjecucion;
    public static final int TIEMPO_CACHE = 60; // una vez por hora
    private final TipoFuente tipoFuente;

    public FuenteProxyDemo(Conexion conexion, URL url) {
        this.conexion = conexion;
        this.url = url;
        this.cacheHechos = List.of();
        this.ultimaEjecucion = null;
        this.tipoFuente = TipoFuente.PROXY;
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

package utn.dds.fuentes.dinamica;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FuenteDinamicaImpl implements FuenteDeDatos {
    private final TipoFuente tipoFuente;
    private final URL url;
    private final Conexion conexion;

    // Aca no se si iria lo de conexion
    public FuenteDinamicaImpl(Conexion conexion, URL url) {
        this.conexion = conexion;
        this.url = url;
        this.tipoFuente = TipoFuente.DINAMICA;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<>();
        Map<String, Object> datos;

        // Empezamos a obtener hechos hasta que no hay más
        do {
            datos = conexion.obtenerDatos(url);
            if (datos != null) {
                Hecho hecho = new Hecho(datos);
                hechos.add(hecho);
            }
        } while (datos != null);

        return hechos;
    }

    @Override
    public TipoFuente tipo() {
        return tipoFuente;
    }
} 
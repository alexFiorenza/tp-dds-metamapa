package utn.dds.dominio.fuentes.proxy;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FuenteDemo implements FuenteDeDatos {
    private final Conexion conexion;
    private final URL url;
    private LocalDateTime fechaUltimaConsulta;
    private final TipoFuente tipoFuente;

    public FuenteDemo(Conexion conexion, URL url) {
        this.conexion = conexion;
        this.url = url;
        this.tipoFuente = TipoFuente.PROXY;
        this.fechaUltimaConsulta = null;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<>();
        Map<String, Object> datos;

        // Empezamos a obtener hechos hasta que no hay más
        do {
            datos = conexion.obtenerDatos(url, fechaUltimaConsulta);
            if (datos != null) {
                Hecho hecho = new Hecho(datos);
                hechos.add(hecho);
                this.fechaUltimaConsulta = LocalDateTime.now();
            }
        } while (datos != null);

        return hechos;
    }
    
    @Override
    public TipoFuente tipo() {
        return this.tipoFuente;
    }
} 
package utn.dds.fuentes.dinamica.conexion;

import java.net.URL;
import java.util.Map;

public interface Conexion {
    Map<String,Object> obtenerDatos(URL url);
}

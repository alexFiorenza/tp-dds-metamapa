package utn.dds.fuentes.dinamica;

import java.net.URL;
import java.util.Map;

public class ConexionEjemplo implements Conexion {
    @Override
    public Map<String, Object> obtenerDatos(URL url) {
        // Devolve un mock de datos por ahora
        return null;
    }
}

package utn.dds.fuentes.proxy.demo.service.model.conexion;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;

public class ConexionExampleIml implements Conexion {
    @Override
    public Map<String, Object> obtenerDatos(URL url, LocalDateTime fechaUltimaConsulta) {
        // Devolve un mock de datos por ahora
        return null;
    }
}

package utn.dds.dominio.fuentes.proxy;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;

public interface Conexion {
    Map<String,Object> obtenerDatos(URL url, LocalDateTime fechaUltimaConsulta);
} 
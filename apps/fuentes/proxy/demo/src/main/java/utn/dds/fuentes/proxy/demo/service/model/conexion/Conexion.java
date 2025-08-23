package utn.dds.fuentes.proxy.demo.service.model.conexion;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;

public interface Conexion {
    Map<String,Object> obtenerDatos(URL url, LocalDateTime fechaUltimaConsulta);
} 
package utn.dds.fuentes.proxy.demo.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.Redis;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EjecucionRepository {
    private final Redis<String> redisClient;
    private final String ULTIMA_EJECUCION_KEY = "fuente_proxy_demo_ultima_ejecucion";
    
    public EjecucionRepository(IDAO<String> dao) {
        this.redisClient = (Redis<String>) dao;
    }

    public LocalDateTime obtenerUltimaEjecucion() {
        String ultimaEjecucionStr = redisClient.findById(ULTIMA_EJECUCION_KEY);
        if (ultimaEjecucionStr != null) {
            return LocalDateTime.parse(ultimaEjecucionStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        return null;
    }

    public void guardarUltimaEjecucion(LocalDateTime ultimaEjecucion) {
        redisClient.save(ULTIMA_EJECUCION_KEY, ultimaEjecucion.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
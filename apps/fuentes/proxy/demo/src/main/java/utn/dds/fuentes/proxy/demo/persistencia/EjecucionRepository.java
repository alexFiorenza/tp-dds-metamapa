package utn.dds.fuentes.proxy.demo.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class EjecucionRepository {
    private final IDAO<String> dao;
    private final String ULTIMA_EJECUCION_KEY = "fuente_proxy_demo_ultima_ejecucion";
    
    public EjecucionRepository(String daoType, Map<String, Object> daoConfig) {
        this.dao = DAOFactory.createDAO(String.class, daoType, daoConfig);
    }

    public LocalDateTime obtenerUltimaEjecucion() {
        // Este es un patch medio rapido para que funcione con Redis. Ver bien como hacerlo.
        if (dao instanceof utn.dds.daos.Redis) {
            String ultimaEjecucionStr = ((utn.dds.daos.Redis<String>) dao).findById(ULTIMA_EJECUCION_KEY);
            if (ultimaEjecucionStr != null) {
                return LocalDateTime.parse(ultimaEjecucionStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        }
        return null;
    }

    public void guardarUltimaEjecucion(LocalDateTime ultimaEjecucion) {
        // Para EjecucionRepository, usamos el patrón de key específica
        if (dao instanceof utn.dds.daos.Redis) {
            ((utn.dds.daos.Redis<String>) dao).save(ULTIMA_EJECUCION_KEY, ultimaEjecucion.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }
}
package utn.dds.fuentes.dinamica.repositories;

import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.SolicitudEliminacionDTO;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class SolicitudEliminacionRepositoryDinamica {
    private final IDAO<SolicitudEliminacionDTO> dao;

    public SolicitudEliminacionRepositoryDinamica(String daoType, Map<String, Object> daoConfig) {
        if ("filesystem".equals(daoType)) {
            Map<String, Object> config = new java.util.HashMap<>();
            config.put("url", "src/main/resources/mocks/solicitudes.json");
            this.dao = DAOFactory.createDAO(SolicitudEliminacionDTO.class, daoType, config);
        } else if ("couchdb".equals(daoType)) {
            // Para CouchDB, crear una DB específica para solicitudes de eliminación
            Map<String, Object> configSolicitudes = new java.util.HashMap<>(daoConfig);
            String baseUrl = (String) configSolicitudes.get("baseUrl");
            String dbPrefix = (String) configSolicitudes.get("dbPrefix");

            // Crear URL completa: http://localhost:5984/dinamica_db_solicitudes
            String fullUrl = baseUrl.endsWith("/")
                ? baseUrl + dbPrefix + "_solicitudes"
                : baseUrl + "/" + dbPrefix + "_solicitudes";

            configSolicitudes.put("url", fullUrl);
            this.dao = DAOFactory.createDAO(SolicitudEliminacionDTO.class, daoType, configSolicitudes);
        } else {
            this.dao = DAOFactory.createDAO(SolicitudEliminacionDTO.class, daoType, daoConfig);
        }
    }

    public List<SolicitudEliminacion> obtenerSolicitudes(){
        List<SolicitudEliminacionDTO> solicitudesDTO = dao.find();
        List<SolicitudEliminacion> solicitudes = solicitudesDTO.stream()
                .map(SolicitudEliminacionDTO::toSolicitudEliminacion)
                .collect(Collectors.toList());
        return solicitudes;
    }

    public SolicitudEliminacion obtenerSolicitud(String uuid) throws IOException{
        List<SolicitudEliminacion> solicitudes = obtenerSolicitudes();

        Optional<SolicitudEliminacion> encontrada = solicitudes.stream()
                .filter(s -> s.getUuid().equals(uuid))
                .findFirst();

        if (encontrada.isPresent()) {
            SolicitudEliminacion solicitud = encontrada.get();
            return solicitud;
        } else {
            throw new NoSuchElementException("No se encontró una solicitud con UUID: " + uuid);
        }
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
        SolicitudEliminacionDTO soliDTO = SolicitudEliminacionDTO.fromSolicitudEliminacion(solicitud);
        this.dao.save(soliDTO);
        return solicitud;
    }

    public SolicitudEliminacion procesarSolicitud(String uuid) throws IOException{
        SolicitudEliminacion solicitud = obtenerSolicitud(uuid);
        solicitud.ocultar();
        SolicitudEliminacionDTO soliDTO = SolicitudEliminacionDTO.fromSolicitudEliminacion(solicitud);
        this.dao.save(soliDTO);
        return solicitud;
    }
}

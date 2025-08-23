package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoSolicitud;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SolicitudEliminacionRepository {
    private final IDAO<SolicitudEliminacion> dao;

    public SolicitudEliminacionRepository(String daoType, Map<String, Object> daoConfig) {
        this.dao = DAOFactory.createDAO(SolicitudEliminacion.class, daoType, daoConfig);
    }

    public List<SolicitudEliminacion> obtenerTodas() {
        return this.dao.find();
    }

    public List<SolicitudEliminacion> obtenerPorEstado(EstadoSolicitud estado) {
        return this.dao.find().stream()
            .filter(s -> s.getEstado() == estado)
            .collect(Collectors.toList());
    }

    public SolicitudEliminacion obtenerPorId(String id) {
        List<SolicitudEliminacion> solicitudes = this.dao.find();
        return solicitudes.stream()
            .filter(s -> s.getUuid().equals(id))
            .findFirst()
            .orElse(null);
    }

    public void crear(SolicitudEliminacion solicitud) {
        this.dao.save(solicitud);
    }

    public void actualizar(String id, SolicitudEliminacion solicitudActualizada) {
        List<SolicitudEliminacion> solicitudes = this.dao.find();
        for (int i = 0; i < solicitudes.size(); i++) {
            if (solicitudes.get(i).getUuid().equals(id)) {
                solicitudes.set(i, solicitudActualizada);
                break;
            }
        }
        this.dao.saveAll(solicitudes);
    }

    public void eliminar(String id) {
        List<SolicitudEliminacion> solicitudes = this.dao.find();
        solicitudes.removeIf(s -> s.getUuid().equals(id));
        this.dao.saveAll(solicitudes);
    }
}
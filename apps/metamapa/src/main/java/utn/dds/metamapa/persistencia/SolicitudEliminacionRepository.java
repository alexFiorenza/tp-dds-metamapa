package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoSolicitud;
import utn.dds.jpa.entities.SolicitudEliminacionEntity;
import utn.dds.mappers.SolicitudEliminacionMapper;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class SolicitudEliminacionRepository {
    private IDAO<SolicitudEliminacionEntity> dao;

    public SolicitudEliminacionRepository() {
        this(new HashMap<>());
    }

    public SolicitudEliminacionRepository(Map<String, Object> daoConfig) {
        Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);

        // Configurar valores por defecto desde variables de entorno
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

        this.dao = DAOFactory.createDAO(SolicitudEliminacionEntity.class, "hibernate", hibernateConfig);
    }

    public List<SolicitudEliminacion> obtenerTodas() {
        List<SolicitudEliminacionEntity> entities = dao.find();
        return entities.stream()
                .map(SolicitudEliminacionMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<SolicitudEliminacion> obtenerPorEstado(EstadoSolicitud estado) {
        List<SolicitudEliminacionEntity> entities = dao.find();
        return entities.stream()
                .map(SolicitudEliminacionMapper::toDomain)
                .filter(s -> s.getEstado() == estado)
                .collect(Collectors.toList());
    }

    public SolicitudEliminacion obtenerPorId(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacionEntity> hibernateDAO = (Hibernate<SolicitudEliminacionEntity>) dao;
            SolicitudEliminacionEntity entity = hibernateDAO.findById(id);
            return entity != null ? SolicitudEliminacionMapper.toDomain(entity) : null;
        }
        return null;
    }

    public void crear(SolicitudEliminacion solicitud) {
        SolicitudEliminacionEntity entity = SolicitudEliminacionMapper.toEntity(solicitud);
        dao.save(entity);
    }

    public void actualizar(String id, SolicitudEliminacion solicitudActualizada) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacionEntity> hibernateDAO = (Hibernate<SolicitudEliminacionEntity>) dao;
            SolicitudEliminacionEntity entity = hibernateDAO.findById(id);
            if (entity != null) {
                entity.setEstado(solicitudActualizada.getEstado());
                dao.save(entity);
            }
        }
    }

    public void eliminar(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacionEntity> hibernateDAO = (Hibernate<SolicitudEliminacionEntity>) dao;
            SolicitudEliminacionEntity entity = hibernateDAO.findById(id);
            if (entity != null) {
                hibernateDAO.delete(entity);
            }
        }
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacionEntity> hibernateDAO = (Hibernate<SolicitudEliminacionEntity>) dao;
            hibernateDAO.close();
        }
    }
}
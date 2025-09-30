package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoSolicitud;
import utn.dds.jpa.entities.SolicitudEliminacionEntity;
import utn.dds.mappers.SolicitudEliminacionMapper;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
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

    public RespuestaPaginadaDTO<SolicitudEliminacionDTO> obtenerTodos(int page, int size) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacionEntity> hibernateDAO = (Hibernate<SolicitudEliminacionEntity>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Consulta para contar total de elementos
                Long totalElements = em.createQuery(
                    "SELECT COUNT(s) FROM SolicitudEliminacionEntity s",
                    Long.class)
                    .getSingleResult();

                // Consulta paginada ordenada por fecha (más recientes primero)
                List<SolicitudEliminacionEntity> entities = em.createQuery(
                    "SELECT s FROM SolicitudEliminacionEntity s ORDER BY s.fechaSolicitud DESC",
                    SolicitudEliminacionEntity.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();

                // Convertir a DTO
                List<SolicitudEliminacionDTO> dtos = entities.stream()
                        .map(entity -> {
                            SolicitudEliminacion solicitud = SolicitudEliminacionMapper.toDomain(entity);
                            return SolicitudEliminacionDTO.fromSolicitudEliminacion(solicitud);
                        })
                        .collect(Collectors.toList());

                return new RespuestaPaginadaDTO<>(dtos, page, size, totalElements);
            });
        }

        // Fallback sin paginación para otros tipos de DAO
        List<SolicitudEliminacionEntity> entities = dao.find();
        List<SolicitudEliminacionDTO> dtos = entities.stream()
                .map(entity -> {
                    SolicitudEliminacion solicitud = SolicitudEliminacionMapper.toDomain(entity);
                    return SolicitudEliminacionDTO.fromSolicitudEliminacion(solicitud);
                })
                .collect(Collectors.toList());

        // Simular paginación manualmente
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, dtos.size());
        List<SolicitudEliminacionDTO> paginatedData = fromIndex < dtos.size() ?
            dtos.subList(fromIndex, toIndex) : new ArrayList<>();

        return new RespuestaPaginadaDTO<>(paginatedData, page, size, dtos.size());
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacionEntity> hibernateDAO = (Hibernate<SolicitudEliminacionEntity>) dao;
            hibernateDAO.close();
        }
    }
}
package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.EstadoSolicitud;
import utn.dds.dominio.Hecho;
import utn.dds.dto.SolicitudEliminacionDTO;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class SolicitudEliminacionRepository {
    private IDAO<SolicitudEliminacion> dao;

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

        this.dao = DAOFactory.createDAO(SolicitudEliminacion.class, "hibernate", hibernateConfig);
    }

    public List<SolicitudEliminacion> obtenerTodas() {
        return dao.find();
    }

    public List<SolicitudEliminacion> obtenerPorEstado(EstadoSolicitud estado) {
        return dao.find().stream()
                .filter(s -> s.getEstado() == estado)
                .collect(Collectors.toList());
    }

    public SolicitudEliminacion obtenerPorId(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacion> hibernateDAO = (Hibernate<SolicitudEliminacion>) dao;
            return hibernateDAO.findById(id);
        }
        return null;
    }

    public void crear(SolicitudEliminacion solicitud) {
        dao.save(solicitud);
    }

    public void actualizar(String id, SolicitudEliminacion solicitudActualizada) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacion> hibernateDAO = (Hibernate<SolicitudEliminacion>) dao;
            SolicitudEliminacion solicitud = hibernateDAO.findById(id);
            if (solicitud != null) {
                solicitud.setEstado(solicitudActualizada.getEstado());
                hibernateDAO.update(solicitud);
            }
        }
    }

    public void eliminar(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacion> hibernateDAO = (Hibernate<SolicitudEliminacion>) dao;
            SolicitudEliminacion solicitud = hibernateDAO.findById(id);
            if (solicitud != null) {
                hibernateDAO.delete(solicitud);
            }
        }
    }

    public RespuestaPaginadaDTO<SolicitudEliminacionDTO> obtenerTodos(int page, int size) {
        if (dao instanceof Hibernate) {
            Hibernate<SolicitudEliminacion> hibernateDAO = (Hibernate<SolicitudEliminacion>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Consulta para contar total de elementos
                Long totalElements = em.createQuery(
                    "SELECT COUNT(s) FROM SolicitudEliminacion s",
                    Long.class)
                    .getSingleResult();

                // Consulta paginada ordenada por fecha (más recientes primero)
                List<SolicitudEliminacion> solicitudes = em.createQuery(
                    "SELECT s FROM SolicitudEliminacion s ORDER BY s.fechaSolicitud DESC",
                    SolicitudEliminacion.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();

                // Convertir a DTO y popular los hechos
                List<SolicitudEliminacionDTO> dtos = solicitudes.stream()
                        .map(solicitud -> {
                            SolicitudEliminacionDTO dto = SolicitudEliminacionDTO.fromSolicitudEliminacion(solicitud);

                            // Obtener el hecho asociado
                            try {
                                Hecho hecho = em.find(Hecho.class, solicitud.getHecho());
                                if (hecho != null) {
                                    dto.setHechoDTO(HechoDTO.fromHecho(hecho));
                                }
                            } catch (Exception e) {
                                // Si no se puede cargar el hecho, continuar sin él
                            }

                            return dto;
                        })
                        .collect(Collectors.toList());

                return new RespuestaPaginadaDTO<>(dtos, page, size, totalElements);
            });
        }

        // Fallback sin paginación para otros tipos de DAO
        List<SolicitudEliminacion> solicitudes = dao.find();
        List<SolicitudEliminacionDTO> dtos = solicitudes.stream()
                .map(SolicitudEliminacionDTO::fromSolicitudEliminacion)
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
            Hibernate<SolicitudEliminacion> hibernateDAO = (Hibernate<SolicitudEliminacion>) dao;
            hibernateDAO.close();
        }
    }
}
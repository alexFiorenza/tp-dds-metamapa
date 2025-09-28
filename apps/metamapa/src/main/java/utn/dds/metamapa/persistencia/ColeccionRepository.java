package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.jpa.entities.ColeccionEntity;
import utn.dds.mappers.ColeccionMapper;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ColeccionRepository {
    private IDAO<ColeccionEntity> dao;

    public ColeccionRepository() {
        this(new HashMap<>());
    }

    public ColeccionRepository(Map<String, Object> daoConfig) {
        Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);

        // Configurar valores por defecto desde variables de entorno
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

        this.dao = DAOFactory.createDAO(ColeccionEntity.class, "hibernate", hibernateConfig);
    }

    public List<Coleccion> obtenerTodas() {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Primera consulta: obtener colecciones básicas con fuentes
                List<ColeccionEntity> entities = em.createQuery(
                    "SELECT DISTINCT c FROM ColeccionEntity c " +
                    "LEFT JOIN FETCH c.fuentes " +
                    "ORDER BY c.titulo",
                    ColeccionEntity.class)
                    .getResultList();

                // Segunda consulta: cargar criterios para las colecciones obtenidas (misma sesión)
                if (!entities.isEmpty()) {
                    List<String> handles = entities.stream()
                            .map(ColeccionEntity::getHandle)
                            .collect(Collectors.toList());

                    em.createQuery(
                        "SELECT DISTINCT c FROM ColeccionEntity c " +
                        "LEFT JOIN FETCH c.criteriosDePertenencia " +
                        "WHERE c.handle IN :handles",
                        ColeccionEntity.class)
                        .setParameter("handles", handles)
                        .getResultList();
                }

                // Convertir a domain dentro de la misma sesión
                return entities.stream()
                        .map(ColeccionMapper::toDomain)
                        .collect(Collectors.toList());
            });
        }

        List<ColeccionEntity> entities = dao.find();
        return entities.stream()
                .map(ColeccionMapper::toDomain)
                .collect(Collectors.toList());
    }

    public List<ColeccionDTO> obtenerTodasDTO() {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Primera consulta: obtener colecciones básicas con fuentes
                List<ColeccionEntity> entities = em.createQuery(
                    "SELECT DISTINCT c FROM ColeccionEntity c " +
                    "LEFT JOIN FETCH c.fuentes " +
                    "ORDER BY c.titulo",
                    ColeccionEntity.class)
                    .getResultList();

                // Segunda consulta: cargar criterios para las colecciones obtenidas (misma sesión)
                if (!entities.isEmpty()) {
                    List<String> handles = entities.stream()
                            .map(ColeccionEntity::getHandle)
                            .collect(Collectors.toList());

                    em.createQuery(
                        "SELECT DISTINCT c FROM ColeccionEntity c " +
                        "LEFT JOIN FETCH c.criteriosDePertenencia " +
                        "WHERE c.handle IN :handles",
                        ColeccionEntity.class)
                        .setParameter("handles", handles)
                        .getResultList();
                }

                // Convertir a DTO dentro de la misma sesión
                return entities.stream()
                        .map(ColeccionMapper::toDTO)
                        .collect(Collectors.toList());
            });
        }

        List<ColeccionEntity> entities = dao.find();
        return entities.stream()
                .map(ColeccionMapper::toDTO)
                .collect(Collectors.toList());
    }

    public RespuestaPaginadaDTO<ColeccionDTO> obtenerTodasDTOPaginado(int page, int size) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Consulta para contar total de elementos
                Long totalElements = em.createQuery(
                    "SELECT COUNT(c) FROM ColeccionEntity c",
                    Long.class)
                    .getSingleResult();

                // Primera consulta: obtener colecciones básicas con fuentes (paginado)
                List<ColeccionEntity> entities = em.createQuery(
                    "SELECT DISTINCT c FROM ColeccionEntity c " +
                    "LEFT JOIN FETCH c.fuentes " +
                    "ORDER BY c.titulo",
                    ColeccionEntity.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();

                // Segunda consulta: cargar criterios para las colecciones obtenidas (misma sesión)
                if (!entities.isEmpty()) {
                    List<String> handles = entities.stream()
                            .map(ColeccionEntity::getHandle)
                            .collect(Collectors.toList());

                    em.createQuery(
                        "SELECT DISTINCT c FROM ColeccionEntity c " +
                        "LEFT JOIN FETCH c.criteriosDePertenencia " +
                        "WHERE c.handle IN :handles",
                        ColeccionEntity.class)
                        .setParameter("handles", handles)
                        .getResultList();
                }

                // Convertir a DTO dentro de la misma sesión
                List<ColeccionDTO> dtos = entities.stream()
                        .map(ColeccionMapper::toDTO)
                        .collect(Collectors.toList());

                return new RespuestaPaginadaDTO<>(dtos, page, size, totalElements);
            });
        }

        // Fallback sin paginación para otros tipos de DAO
        List<ColeccionEntity> entities = dao.find();
        List<ColeccionDTO> dtos = entities.stream()
                .map(ColeccionMapper::toDTO)
                .collect(Collectors.toList());

        // Simular paginación manualmente
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, dtos.size());
        List<ColeccionDTO> paginatedData = fromIndex < dtos.size() ?
            dtos.subList(fromIndex, toIndex) : new ArrayList<>();

        return new RespuestaPaginadaDTO<>(paginatedData, page, size, dtos.size());
    }

    public Coleccion obtenerPorId(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            ColeccionEntity entity = hibernateDAO.executeQuery(em -> {
                // Primera consulta: obtener colección básica con hechos
                ColeccionEntity coleccion = em.createQuery(
                    "SELECT c FROM ColeccionEntity c " +
                    "LEFT JOIN FETCH c.hechos " +
                    "WHERE c.handle = :id",
                    ColeccionEntity.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                if (coleccion != null) {
                    // Segunda consulta: cargar fuentes
                    em.createQuery(
                        "SELECT c FROM ColeccionEntity c " +
                        "LEFT JOIN FETCH c.fuentes " +
                        "WHERE c.handle = :id",
                        ColeccionEntity.class)
                        .setParameter("id", id)
                        .getResultList();

                    // Tercera consulta: cargar criterios de pertenencia
                    em.createQuery(
                        "SELECT c FROM ColeccionEntity c " +
                        "LEFT JOIN FETCH c.criteriosDePertenencia " +
                        "WHERE c.handle = :id",
                        ColeccionEntity.class)
                        .setParameter("id", id)
                        .getResultList();
                }

                return coleccion;
            });
            return entity != null ? ColeccionMapper.toDomain(entity) : null;
        }
        return null;
    }

    public ColeccionDTO obtenerDTOPorId(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            ColeccionEntity entity = hibernateDAO.executeQuery(em -> {
                // Primera consulta: obtener colección básica
                ColeccionEntity coleccion = em.createQuery(
                    "SELECT c FROM ColeccionEntity c WHERE c.handle = :id",
                    ColeccionEntity.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                if (coleccion != null) {
                    // Segunda consulta: cargar fuentes
                    em.createQuery(
                        "SELECT c FROM ColeccionEntity c " +
                        "LEFT JOIN FETCH c.fuentes " +
                        "WHERE c.handle = :id",
                        ColeccionEntity.class)
                        .setParameter("id", id)
                        .getResultList();

                    // Tercera consulta: cargar criterios de pertenencia
                    em.createQuery(
                        "SELECT c FROM ColeccionEntity c " +
                        "LEFT JOIN FETCH c.criteriosDePertenencia " +
                        "WHERE c.handle = :id",
                        ColeccionEntity.class)
                        .setParameter("id", id)
                        .getResultList();
                }

                return coleccion;
            });
            return entity != null ? ColeccionMapper.toDTO(entity) : null;
        }
        return null;
    }

    public List<Hecho> obtenerHechos(String handle) {
        Coleccion coleccion = obtenerPorId(handle);
        if (coleccion == null) {
            return new ArrayList<>();
        }

        List<Hecho> todosLosHechos = coleccion.getHechos();
        List<HechoStrategy> criterios = coleccion.getCriteriosDePertenencia();

        // Si no hay criterios, devolver todos los hechos
        if (criterios == null || criterios.isEmpty()) {
            return todosLosHechos;
        }

        // Aplicar criterios de pertenencia automáticamente
        return todosLosHechos.stream()
                .filter(hecho -> criterios.stream().allMatch(criterio -> criterio.cumple(hecho)))
                .collect(Collectors.toList());
    }

    public void crear(Coleccion coleccion) {
        ColeccionEntity entity = ColeccionMapper.toEntity(coleccion);
        dao.save(entity);
    }

    public void actualizar(String id, Coleccion coleccionActualizada) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            ColeccionEntity entity = hibernateDAO.findById(id);
            if (entity != null) {
                entity.setTitulo(coleccionActualizada.getTitulo());
                entity.setDescripcion(coleccionActualizada.getDescripcion());
                dao.save(entity);
            }
        }
    }

    public void eliminar(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;

            // Usar executeQuery para operaciones complejas con manejo manual de transacciones
            hibernateDAO.executeQuery(em -> {
                em.getTransaction().begin();
                try {
                    // Buscar la colección con sus relaciones, pero hacerlo por separado para evitar MultipleBagFetchException
                    ColeccionEntity entity = em.createQuery(
                        "SELECT c FROM ColeccionEntity c WHERE c.handle = :id",
                        ColeccionEntity.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                    if (entity != null) {
                        // Cargar las relaciones de forma separada para poder limpiarlas

                        // Cargar y limpiar hechos
                        entity = em.createQuery(
                            "SELECT c FROM ColeccionEntity c LEFT JOIN FETCH c.hechos WHERE c.handle = :id",
                            ColeccionEntity.class)
                            .setParameter("id", id)
                            .getSingleResult();
                        entity.getHechos().clear();

                        // Cargar y limpiar fuentes
                        entity = em.createQuery(
                            "SELECT c FROM ColeccionEntity c LEFT JOIN FETCH c.fuentes WHERE c.handle = :id",
                            ColeccionEntity.class)
                            .setParameter("id", id)
                            .getSingleResult();
                        entity.getFuentes().clear();

                        // Eliminar criterios explícitamente
                        em.createQuery("DELETE FROM CriterioEntity c WHERE c.idColeccion = :handle")
                            .setParameter("handle", id)
                            .executeUpdate();

                        em.flush(); // Sincronizar cambios de las relaciones

                        // Ahora eliminar la entidad principal
                        em.remove(entity);
                    }

                    em.getTransaction().commit();
                    return null;
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw new RuntimeException("Error al eliminar colección", e);
                }
            });
        }
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            hibernateDAO.close();
        }
    }
}
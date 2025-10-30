package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ColeccionRepository {
    private IDAO<Coleccion> dao;

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

        this.dao = DAOFactory.createDAO(Coleccion.class, "hibernate", hibernateConfig);
    }

    public RespuestaPaginadaDTO<Coleccion> obtenerTodas(int page, int size) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Consulta para contar total de elementos
                Long totalElements = em.createQuery(
                    "SELECT COUNT(c) FROM Coleccion c",
                    Long.class)
                    .getSingleResult();

                // Primera consulta: obtener colecciones básicas con fuentes (paginado)
                List<Coleccion> entities = em.createQuery(
                    "SELECT DISTINCT c FROM Coleccion c " +
                    "LEFT JOIN FETCH c.fuentes " +
                    "ORDER BY c.titulo",
                    Coleccion.class)
                    .setFirstResult(page * size)
                    .setMaxResults(size)
                    .getResultList();

                // Segunda consulta: cargar criterios para las colecciones obtenidas (misma sesión)
                if (!entities.isEmpty()) {
                    List<String> handles = entities.stream()
                            .map(Coleccion::getHandle)
                            .collect(Collectors.toList());

                    em.createQuery(
                        "SELECT DISTINCT c FROM Coleccion c " +
                        "LEFT JOIN FETCH c.criteriosDePertenencia " +
                        "WHERE c.handle IN :handles",
                        Coleccion.class)
                        .setParameter("handles", handles)
                        .getResultList();
                }

                return new RespuestaPaginadaDTO<>(entities, page, size, totalElements);
            });
        }

        // Fallback sin paginación para otros tipos de DAO
        List<Coleccion> entities = dao.find();

        // Simular paginación manualmente
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, entities.size());
        List<Coleccion> paginatedData = fromIndex < entities.size() ?
            entities.subList(fromIndex, toIndex) : new ArrayList<>();

        return new RespuestaPaginadaDTO<>(paginatedData, page, size, entities.size());
    }

    public Coleccion obtenerPorId(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;
            return hibernateDAO.executeQuery(em -> {
                // Primera consulta: obtener colección básica con hechos
                Coleccion coleccion = em.createQuery(
                    "SELECT c FROM Coleccion c " +
                    "LEFT JOIN FETCH c.hechos " +
                    "WHERE c.handle = :id",
                    Coleccion.class)
                    .setParameter("id", id)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

                if (coleccion != null) {
                    // Segunda consulta: cargar fuentes
                    em.createQuery(
                        "SELECT c FROM Coleccion c " +
                        "LEFT JOIN FETCH c.fuentes " +
                        "WHERE c.handle = :id",
                        Coleccion.class)
                        .setParameter("id", id)
                        .getResultList();

                    // Tercera consulta: cargar criterios de pertenencia
                    em.createQuery(
                        "SELECT c FROM Coleccion c " +
                        "LEFT JOIN FETCH c.criteriosDePertenencia " +
                        "WHERE c.handle = :id",
                        Coleccion.class)
                        .setParameter("id", id)
                        .getResultList();
                }

                return coleccion;
            });
        }
        return null;
    }

    public List<Hecho> obtenerHechos(String handle) {
        Coleccion entity = obtenerPorId(handle);
        if (entity == null) {
            return new ArrayList<>();
        }

        List<Hecho> todosLosHechos = entity.getHechos();
        List<HechoStrategy> criterios = entity.getCriteriosDePertenenciaAsStrategies();

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
        dao.save(coleccion);
    }

    public void actualizar(String id, Coleccion coleccionActualizada) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;
            Coleccion entity = hibernateDAO.findById(id);
            if (entity != null) {
                entity.setTitulo(coleccionActualizada.getTitulo());
                entity.setDescripcion(coleccionActualizada.getDescripcion());
                dao.save(entity);
            }
        }
    }

    public void actualizarCamposBasicos(String id, String titulo, String descripcion) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;
            hibernateDAO.executeQuery(em -> {
                em.getTransaction().begin();
                try {
                    Coleccion entity = em.find(Coleccion.class, id);
                    if (entity != null) {
                        if (titulo != null) entity.setTitulo(titulo);
                        if (descripcion != null) entity.setDescripcion(descripcion);
                        em.merge(entity);
                    }
                    em.getTransaction().commit();
                    return null;
                } catch (Exception e) {
                    em.getTransaction().rollback();
                    throw new RuntimeException("Error al actualizar campos básicos", e);
                }
            });
        }
    }

    public void actualizarCriterios(String id, List<utn.dds.dominio.criterios.HechoStrategy> nuevosCriterios) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;
            hibernateDAO.executeQuery(em -> {
                em.getTransaction().begin();
                try {
                    // Eliminar criterios antiguos
                    em.createQuery("DELETE FROM Criterio c WHERE c.idColeccion = :handle")
                        .setParameter("handle", id)
                        .executeUpdate();

                    // Crear nuevos criterios si existen
                    if (nuevosCriterios != null && !nuevosCriterios.isEmpty()) {
                        for (HechoStrategy strategy : nuevosCriterios) {
                            utn.dds.dominio.Criterio criterio = utn.dds.dominio.Criterio.fromHechoStrategy(strategy, id);
                            em.persist(criterio);
                        }
                    }

                    em.getTransaction().commit();
                    return null;
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw new RuntimeException("Error al actualizar criterios", e);
                }
            });
        }
    }

    public void actualizarFuentes(String id, List<utn.dds.dominio.Fuente> nuevasFuentes) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;
            hibernateDAO.executeQuery(em -> {
                em.getTransaction().begin();
                try {
                    // Cargar la colección con fuentes
                    Coleccion entity = em.createQuery(
                        "SELECT c FROM Coleccion c LEFT JOIN FETCH c.fuentes WHERE c.handle = :id",
                        Coleccion.class)
                        .setParameter("id", id)
                        .getSingleResult();

                    // Actualizar fuentes
                    entity.setFuentes(nuevasFuentes);
                    em.merge(entity);

                    em.getTransaction().commit();
                    return null;
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw new RuntimeException("Error al actualizar fuentes", e);
                }
            });
        }
    }

    public void actualizarHechos(String id, List<utn.dds.dominio.Hecho> nuevosHechos) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;
            hibernateDAO.executeQuery(em -> {
                em.getTransaction().begin();
                try {
                    // Cargar la colección con hechos
                    Coleccion entity = em.createQuery(
                        "SELECT c FROM Coleccion c LEFT JOIN FETCH c.hechos WHERE c.handle = :id",
                        Coleccion.class)
                        .setParameter("id", id)
                        .getSingleResult();

                    // Reemplazar hechos
                    entity.setHechos(nuevosHechos);
                    em.merge(entity);

                    em.getTransaction().commit();
                    return null;
                } catch (Exception e) {
                    if (em.getTransaction().isActive()) {
                        em.getTransaction().rollback();
                    }
                    throw new RuntimeException("Error al actualizar hechos", e);
                }
            });
        }
    }

    public void eliminar(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;

            // Usar executeQuery para operaciones complejas con manejo manual de transacciones
            hibernateDAO.executeQuery(em -> {
                em.getTransaction().begin();
                try {
                    // Buscar la colección con sus relaciones, pero hacerlo por separado para evitar MultipleBagFetchException
                    Coleccion entity = em.createQuery(
                        "SELECT c FROM Coleccion c WHERE c.handle = :id",
                        Coleccion.class)
                        .setParameter("id", id)
                        .getResultStream()
                        .findFirst()
                        .orElse(null);

                    if (entity != null) {
                        // Cargar las relaciones de forma separada para poder limpiarlas

                        // Cargar y limpiar hechos
                        entity = em.createQuery(
                            "SELECT c FROM Coleccion c LEFT JOIN FETCH c.hechos WHERE c.handle = :id",
                            Coleccion.class)
                            .setParameter("id", id)
                            .getSingleResult();
                        entity.getHechos().clear();

                        // Cargar y limpiar fuentes
                        entity = em.createQuery(
                            "SELECT c FROM Coleccion c LEFT JOIN FETCH c.fuentes WHERE c.handle = :id",
                            Coleccion.class)
                            .setParameter("id", id)
                            .getSingleResult();
                        entity.getFuentes().clear();

                        // Eliminar criterios explícitamente
                        em.createQuery("DELETE FROM Criterio c WHERE c.idColeccion = :handle")
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
            Hibernate<Coleccion> hibernateDAO = (Hibernate<Coleccion>) dao;
            hibernateDAO.close();
        }
    }
}

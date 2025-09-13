package utn.dds.daos;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class HibernateDAO<T> implements IDAO<T> {
    private static final Logger logger = LoggerFactory.getLogger(HibernateDAO.class);
    
    private final Class<T> entityClass;
    private final EntityManagerFactory entityManagerFactory;
    private final String persistenceUnitName;

    public HibernateDAO(Class<T> entityClass, Map<String, Object> config) {
        this.entityClass = entityClass;
        this.persistenceUnitName = (String) config.getOrDefault("persistenceUnit", "metamapa-db");
        
        try {
            logger.info("Inicializando HibernateDAO para entidad: {}", entityClass.getSimpleName());
            logger.info("Usando persistence unit: {}", persistenceUnitName);
            
            this.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName, config);
            logger.info("EntityManagerFactory creado exitosamente");
        } catch (Exception e) {
            logger.error("Error al crear EntityManagerFactory: {}", e.getMessage(), e);
            throw new RuntimeException("Error al inicializar HibernateDAO", e);
        }
    }

    public HibernateDAO(Class<T> entityClass, String persistenceUnitName) {
        this.entityClass = entityClass;
        this.persistenceUnitName = persistenceUnitName;
        
        try {
            logger.info("Inicializando HibernateDAO para entidad: {}", entityClass.getSimpleName());
            logger.info("Usando persistence unit: {}", persistenceUnitName);
            
            this.entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
            logger.info("EntityManagerFactory creado exitosamente");
        } catch (Exception e) {
            logger.error("Error al crear EntityManagerFactory: {}", e.getMessage(), e);
            throw new RuntimeException("Error al inicializar HibernateDAO", e);
        }
    }

    @Override
    public InputStream read() {
        throw new UnsupportedOperationException("HibernateDAO no soporta operaciones de InputStream. Use find() para obtener entidades.");
    }

    @Override
    public InputStream read(String path) {
        throw new UnsupportedOperationException("HibernateDAO no soporta operaciones de InputStream. Use find() para obtener entidades.");
    }

    @Override
    public List<T> find() {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            logger.debug("Buscando todas las entidades de tipo: {}", entityClass.getSimpleName());
            
            String jpql = "SELECT e FROM " + entityClass.getSimpleName() + " e";
            TypedQuery<T> query = em.createQuery(jpql, entityClass);
            List<T> results = query.getResultList();
            
            logger.debug("Encontradas {} entidades", results.size());
            return results;
        } catch (Exception e) {
            logger.error("Error al buscar entidades: {}", e.getMessage(), e);
            throw new RuntimeException("Error al buscar entidades", e);
        } finally {
            em.close();
        }
    }

    public T findById(Object id) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            logger.debug("Buscando entidad {} con ID: {}", entityClass.getSimpleName(), id);
            
            T entity = em.find(entityClass, id);
            if (entity != null) {
                logger.debug("Entidad encontrada: {}", entity);
            } else {
                logger.debug("No se encontró entidad con ID: {}", id);
            }
            
            return entity;
        } catch (Exception e) {
            logger.error("Error al buscar entidad por ID: {}", e.getMessage(), e);
            throw new RuntimeException("Error al buscar entidad por ID", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void save(T object) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            logger.debug("Guardando entidad: {}", object);
            
            em.getTransaction().begin();
            em.persist(object);
            em.getTransaction().commit();
            
            logger.debug("Entidad guardada exitosamente");
        } catch (Exception e) {
            logger.error("Error al guardar entidad: {}", e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al guardar entidad", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void saveAll(List<T> objects) {
        if (objects == null || objects.isEmpty()) {
            logger.debug("Lista vacía, no hay nada que guardar");
            return;
        }

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            logger.debug("Guardando {} entidades", objects.size());
            
            em.getTransaction().begin();
            
            for (int i = 0; i < objects.size(); i++) {
                em.persist(objects.get(i));
                
                // Flush en lotes para mejorar rendimiento
                if (i % 50 == 0) {
                    em.flush();
                    em.clear();
                }
            }
            
            em.getTransaction().commit();
            logger.debug("Todas las entidades guardadas exitosamente");
        } catch (Exception e) {
            logger.error("Error al guardar entidades en lote: {}", e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al guardar entidades en lote", e);
        } finally {
            em.close();
        }
    }

    @Override
    public void addAll(List<T> objects) {
        // En Hibernate, addAll puede ser lo mismo que saveAll para nuevas entidades
        // o merge para entidades existentes
        if (objects == null || objects.isEmpty()) {
            logger.debug("Lista vacía, no hay nada que agregar");
            return;
        }

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            logger.debug("Agregando {} entidades (merge)", objects.size());
            
            em.getTransaction().begin();
            
            for (int i = 0; i < objects.size(); i++) {
                em.merge(objects.get(i));
                
                // Flush en lotes para mejorar rendimiento
                if (i % 50 == 0) {
                    em.flush();
                    em.clear();
                }
            }
            
            em.getTransaction().commit();
            logger.debug("Todas las entidades agregadas exitosamente");
        } catch (Exception e) {
            logger.error("Error al agregar entidades: {}", e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al agregar entidades", e);
        } finally {
            em.close();
        }
    }

    public void update(T object) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            logger.debug("Actualizando entidad: {}", object);
            
            em.getTransaction().begin();
            em.merge(object);
            em.getTransaction().commit();
            
            logger.debug("Entidad actualizada exitosamente");
        } catch (Exception e) {
            logger.error("Error al actualizar entidad: {}", e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al actualizar entidad", e);
        } finally {
            em.close();
        }
    }

    public void delete(T object) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            logger.debug("Eliminando entidad: {}", object);
            
            em.getTransaction().begin();
            T managedEntity = em.merge(object);
            em.remove(managedEntity);
            em.getTransaction().commit();
            
            logger.debug("Entidad eliminada exitosamente");
        } catch (Exception e) {
            logger.error("Error al eliminar entidad: {}", e.getMessage(), e);
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new RuntimeException("Error al eliminar entidad", e);
        } finally {
            em.close();
        }
    }

    public void close() {
        if (entityManagerFactory != null && entityManagerFactory.isOpen()) {
            logger.info("Cerrando EntityManagerFactory");
            entityManagerFactory.close();
        }
    }

    // Getters para debugging
    public Class<T> getEntityClass() {
        return entityClass;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }
}
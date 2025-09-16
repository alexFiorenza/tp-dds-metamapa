package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dto.FuenteDTO;
import utn.dds.jpa.entities.FuenteEntity;
import utn.dds.mappers.FuenteMapper;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;

public class FuentesRepository {

    private IDAO<FuenteEntity> dao;

    public FuentesRepository() {
        this(new HashMap<>());
    }

    public FuentesRepository(Map<String, Object> daoConfig) {
        Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);

        // Configurar valores por defecto desde variables de entorno
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

        this.dao = DAOFactory.createDAO(FuenteEntity.class, "hibernate", hibernateConfig);
    }
    
    public List<FuenteDTO> find() {
        if (dao instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<FuenteEntity> entityGraph = createEntityGraphWithParams(em);

                return em.createQuery("SELECT DISTINCT f FROM FuenteEntity f", FuenteEntity.class)
                         .setHint("jakarta.persistence.fetchgraph", entityGraph)
                         .getResultList()
                         .stream()
                         .map(FuenteMapper::toDomain)
                         .collect(Collectors.toList());
            });
        }
        return null;
    }
    
    public void save(FuenteDTO fuente) {
        if (fuente.getUuid() == null) {
            fuente.setUuid(UUID.randomUUID());
        }

        FuenteEntity entity = FuenteMapper.toEntity(fuente);
        dao.save(entity);
    }
    
    public FuenteDTO findByHost(String host) {
        if (dao instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<FuenteEntity> entityGraph = createEntityGraphWithParams(em);

                List<FuenteEntity> results = em.createQuery("SELECT DISTINCT f FROM FuenteEntity f WHERE f.host = :host", FuenteEntity.class)
                        .setParameter("host", host)
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList();
                return results.isEmpty() ? null : FuenteMapper.toDomain(results.get(0));
            });
        }
        return null;
    }

    public List<FuenteDTO> findAllByHost(String host) {
        if (dao instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<FuenteEntity> entityGraph = createEntityGraphWithParams(em);

                return em.createQuery("SELECT DISTINCT f FROM FuenteEntity f WHERE f.host = :host", FuenteEntity.class)
                        .setParameter("host", host)
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList()
                        .stream()
                        .map(FuenteMapper::toDomain)
                        .collect(Collectors.toList());
            });
        }
        return null;
    }
    
    public boolean removeByHost(String host) {
        if (dao instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<FuenteEntity> entityGraph = createEntityGraphWithParams(em);

                List<FuenteEntity> toRemove = em.createQuery("SELECT DISTINCT f FROM FuenteEntity f WHERE f.host = :host", FuenteEntity.class)
                        .setParameter("host", host)
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList();

                boolean removed = !toRemove.isEmpty();
                for (FuenteEntity entity : toRemove) {
                    hibernateDAO.delete(entity);
                }
                return removed;
            });
        }
        return false;
    }

    public boolean removeByUuid(UUID uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) dao;
            FuenteEntity entity = hibernateDAO.findById(uuid.toString());
            if (entity != null) {
                hibernateDAO.delete(entity);
                return true;
            }
        }
        return false;
    }

    public FuenteDTO findByUuid(UUID uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<FuenteEntity> entityGraph = createEntityGraphWithParams(em);

                List<FuenteEntity> results = em.createQuery("SELECT DISTINCT f FROM FuenteEntity f WHERE f.uuid = :uuid", FuenteEntity.class)
                        .setParameter("uuid", uuid.toString())
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList();
                return results.isEmpty() ? null : FuenteMapper.toDomain(results.get(0));
            });
        }
        return null;
    }

    private EntityGraph<FuenteEntity> createEntityGraphWithParams(jakarta.persistence.EntityManager em) {
        EntityGraph<FuenteEntity> entityGraph = em.createEntityGraph(FuenteEntity.class);
        entityGraph.addAttributeNodes("params");
        return entityGraph;
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) dao;
            hibernateDAO.close();
        }
    }
}
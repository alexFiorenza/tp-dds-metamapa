package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.HibernateDAO;
import utn.dds.dto.FuenteDTO;
import utn.dds.jpa.entities.FuenteEntity;
import utn.dds.mappers.FuenteMapper;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.io.InputStream;

public class FuentesRepository {

    private HibernateDAO<FuenteEntity> hibernateDAO;  // DAO para entidades JPA
    private IDAO<FuenteDTO> dao;    // DAO para DTOs (filesystem y s3)
    private String daoType;

    public FuentesRepository() {
        // Constructor por defecto que usa configuración por defecto
        this("filesystem", new HashMap<>());
    }

    public FuentesRepository(String daoType, Map<String, Object> daoConfig) {
        this.daoType = daoType;

        if ("hibernate".equals(daoType)) {
            // Para Hibernate, crear DAO que trabaja con entidades JPA
            Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);

            // Configurar valores por defecto desde variables de entorno
            hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
                System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
            hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
                System.getenv().getOrDefault("DB_USER", "metamapa"));
            hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
                System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
            hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

            this.hibernateDAO = new HibernateDAO<>(FuenteEntity.class, hibernateConfig);
            this.dao = null;
        } else if ("filesystem".equals(daoType)) {
            // Para filesystem, usar configuración específica
            Map<String, Object> config = new HashMap<>();
            config.put("url", "src/main/resources/mocks/fuentes.json");
            this.dao = DAOFactory.createDAO(FuenteDTO.class, daoType, config);
            this.hibernateDAO = null;
        } else {
            // Para otros tipos de DAO, usar la configuración provista
            this.dao = DAOFactory.createDAO(FuenteDTO.class, daoType, daoConfig);
            this.hibernateDAO = null;
        }
    }
    
    public List<FuenteDTO> find() {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, usar consulta específica para cargar params
            return findWithHibernate();
        } else {
            // Para otros DAOs, usar DTOs
            return dao.find();
        }
    }

    private List<FuenteDTO> findWithHibernate() {
        return hibernateDAO.executeQuery(em -> {
            String jpql = "SELECT DISTINCT f FROM FuenteEntity f LEFT JOIN FETCH f.params";
            return em.createQuery(jpql, FuenteEntity.class)
                     .getResultList()
                     .stream()
                     .map(FuenteMapper::toDomain)
                     .collect(Collectors.toList());
        });
    }
    
    public void save(FuenteDTO fuente) {
        if (fuente.getUuid() == null) {
            fuente.setUuid(UUID.randomUUID());
        }

        if ("hibernate".equals(daoType)) {
            // Para Hibernate, convertir a entidad JPA y guardar
            FuenteEntity entity = FuenteMapper.toEntity(fuente);
            hibernateDAO.save(entity);
        } else {
            // Para el repositorio de fuentes, necesitamos manejar la lista completa
            // ya que el DAO no tiene métodos específicos para búsqueda por campo
            List<FuenteDTO> fuentes = dao.find();

            // Agregar la fuente (la validación de duplicados se hace en el service)
            fuentes.add(fuente);
            dao.saveAll(fuentes);
        }
    }
    
    public FuenteDTO findByHost(String host) {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, usar consulta específica
            return hibernateDAO.executeQuery(em -> {
                String jpql = "SELECT DISTINCT f FROM FuenteEntity f LEFT JOIN FETCH f.params WHERE f.host = :host";
                List<FuenteEntity> results = em.createQuery(jpql, FuenteEntity.class)
                        .setParameter("host", host)
                        .getResultList();
                return results.isEmpty() ? null : FuenteMapper.toDomain(results.get(0));
            });
        } else {
            // Para otros DAOs, usar DTOs
            return dao.find().stream()
                    .filter(f -> f.getHost().equals(host))
                    .findFirst()
                    .orElse(null);
        }
    }

    public List<FuenteDTO> findAllByHost(String host) {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, usar consulta específica
            return hibernateDAO.executeQuery(em -> {
                String jpql = "SELECT DISTINCT f FROM FuenteEntity f LEFT JOIN FETCH f.params WHERE f.host = :host";
                return em.createQuery(jpql, FuenteEntity.class)
                        .setParameter("host", host)
                        .getResultList()
                        .stream()
                        .map(FuenteMapper::toDomain)
                        .collect(Collectors.toList());
            });
        } else {
            // Para otros DAOs, usar DTOs
            return dao.find().stream()
                    .filter(f -> f.getHost().equals(host))
                    .collect(Collectors.toList());
        }
    }
    
    public boolean removeByHost(String host) {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, buscar y eliminar entidades por host
            return hibernateDAO.executeQuery(em -> {
                String jpql = "SELECT DISTINCT f FROM FuenteEntity f LEFT JOIN FETCH f.params WHERE f.host = :host";
                List<FuenteEntity> toRemove = em.createQuery(jpql, FuenteEntity.class)
                        .setParameter("host", host)
                        .getResultList();

                boolean removed = !toRemove.isEmpty();
                for (FuenteEntity entity : toRemove) {
                    hibernateDAO.delete(entity);
                }
                return removed;
            });
        } else {
            // Para otros DAOs, usar DTOs
            List<FuenteDTO> fuentes = dao.find();
            boolean removed = fuentes.removeIf(f -> f.getHost().equals(host));
            if (removed) {
                dao.saveAll(fuentes);
            }
            return removed;
        }
    }

    public boolean removeByUuid(UUID uuid) {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, eliminar por UUID
            FuenteEntity entity = hibernateDAO.findById(uuid.toString());
            if (entity != null) {
                hibernateDAO.delete(entity);
                return true;
            }
            return false;
        } else {
            // Para otros DAOs, usar DTOs
            List<FuenteDTO> fuentes = dao.find();
            boolean removed = fuentes.removeIf(f -> f.getUuid().equals(uuid));
            if (removed) {
                dao.saveAll(fuentes);
            }
            return removed;
        }
    }

    public FuenteDTO findByUuid(UUID uuid) {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, usar consulta específica para cargar params
            return hibernateDAO.executeQuery(em -> {
                String jpql = "SELECT DISTINCT f FROM FuenteEntity f LEFT JOIN FETCH f.params WHERE f.uuid = :uuid";
                List<FuenteEntity> results = em.createQuery(jpql, FuenteEntity.class)
                        .setParameter("uuid", uuid.toString())
                        .getResultList();
                return results.isEmpty() ? null : FuenteMapper.toDomain(results.get(0));
            });
        } else {
            // Para otros DAOs, usar DTOs
            return dao.find().stream()
                    .filter(f -> f.getUuid().equals(uuid))
                    .findFirst()
                    .orElse(null);
        }
    }

    public void close() {
        if (hibernateDAO != null) {
            hibernateDAO.close();
        }
    }
}
package utn.dds.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dominio.Fuente;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;

public class FuentesRepository {

    private IDAO<Fuente> dao;

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

        this.dao = DAOFactory.createDAO(Fuente.class, "hibernate", hibernateConfig);
    }

    public List<FuenteDTO> find() {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<Fuente> entityGraph = createEntityGraphWithParams(em);

                return em.createQuery("SELECT DISTINCT f FROM Fuente f", Fuente.class)
                         .setHint("jakarta.persistence.fetchgraph", entityGraph)
                         .getResultList()
                         .stream()
                         .map(FuenteDTO::from)
                         .collect(Collectors.toList());
            });
        }
        return null;
    }

    public void save(FuenteDTO fuente) {
        if (fuente.getUuid() == null) {
            fuente.setUuid(UUID.randomUUID());
        }
        dao.save(fuente.toFuente());
    }

    public FuenteDTO findByHost(String host) {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<Fuente> entityGraph = createEntityGraphWithParams(em);

                List<Fuente> results = em.createQuery("SELECT DISTINCT f FROM Fuente f WHERE f.host = :host", Fuente.class)
                        .setParameter("host", host)
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList();
                return results.isEmpty() ? null : FuenteDTO.from(results.get(0));
            });
        }
        return null;
    }

    public List<FuenteDTO> findAllByHost(String host) {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<Fuente> entityGraph = createEntityGraphWithParams(em);

                return em.createQuery("SELECT DISTINCT f FROM Fuente f WHERE f.host = :host", Fuente.class)
                        .setParameter("host", host)
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList()
                        .stream()
                        .map(FuenteDTO::from)
                        .collect(Collectors.toList());
            });
        }
        return null;
    }

    public boolean removeByHost(String host) {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<Fuente> entityGraph = createEntityGraphWithParams(em);

                List<Fuente> toRemove = em.createQuery("SELECT DISTINCT f FROM Fuente f WHERE f.host = :host", Fuente.class)
                        .setParameter("host", host)
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList();

                boolean removed = !toRemove.isEmpty();
                for (Fuente entity : toRemove) {
                    hibernateDAO.delete(entity);
                }
                return removed;
            });
        }
        return false;
    }

    public boolean removeByUuid(UUID uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            Fuente entity = hibernateDAO.findById(uuid.toString());
            if (entity != null) {
                hibernateDAO.delete(entity);
                return true;
            }
        }
        return false;
    }

    public FuenteDTO findByUuid(UUID uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            return hibernateDAO.executeQuery(em -> {
                EntityGraph<Fuente> entityGraph = createEntityGraphWithParams(em);

                List<Fuente> results = em.createQuery("SELECT DISTINCT f FROM Fuente f WHERE f.uuid = :uuid", Fuente.class)
                        .setParameter("uuid", uuid.toString())
                        .setHint("jakarta.persistence.fetchgraph", entityGraph)
                        .getResultList();
                return results.isEmpty() ? null : FuenteDTO.from(results.get(0));
            });
        }
        return null;
    }

    public RespuestaPaginadaDTO<FuenteDTO> findPaginado(int pagina, int tamanioPagina) {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            return hibernateDAO.executeQuery(em -> {
                CriteriaBuilder cb = em.getCriteriaBuilder();

                // Query para contar total
                CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
                Root<Fuente> countRoot = countQuery.from(Fuente.class);
                countQuery.select(cb.count(countRoot));
                Long totalElementos = em.createQuery(countQuery).getSingleResult();

                // Query para obtener datos paginados
                TypedQuery<Fuente> dataQuery = em.createQuery("SELECT DISTINCT f FROM Fuente f", Fuente.class);
                EntityGraph<Fuente> entityGraph = createEntityGraphWithParams(em);
                dataQuery.setHint("jakarta.persistence.fetchgraph", entityGraph);
                dataQuery.setFirstResult(pagina * tamanioPagina);
                dataQuery.setMaxResults(tamanioPagina);

                List<FuenteDTO> fuentes = dataQuery.getResultList()
                        .stream()
                        .map(FuenteDTO::from)
                        .collect(Collectors.toList());

                return new RespuestaPaginadaDTO<>(fuentes, pagina, tamanioPagina, totalElementos);
            });
        }

        // Fallback para otros tipos de DAO
        List<FuenteDTO> todasLasFuentes = find();
        long totalElementos = todasLasFuentes != null ? todasLasFuentes.size() : 0;
        int inicio = pagina * tamanioPagina;
        int fin = Math.min(inicio + tamanioPagina, (int) totalElementos);
        List<FuenteDTO> fuentesPaginadas = todasLasFuentes != null ?
                todasLasFuentes.subList(inicio, fin) : List.of();

        return new RespuestaPaginadaDTO<>(fuentesPaginadas, pagina, tamanioPagina, totalElementos);
    }

    private EntityGraph<Fuente> createEntityGraphWithParams(jakarta.persistence.EntityManager em) {
        EntityGraph<Fuente> entityGraph = em.createEntityGraph(Fuente.class);
        entityGraph.addAttributeNodes("params");
        return entityGraph;
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) dao;
            hibernateDAO.close();
        }
    }
}

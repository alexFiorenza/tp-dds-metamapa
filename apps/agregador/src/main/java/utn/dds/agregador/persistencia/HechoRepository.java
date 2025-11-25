package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.persistencia.StrategyToSQLAdapter;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;
import utn.dds.dominio.Contribuyente;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.TypedQuery;
public class HechoRepository {

    private IDAO<Hecho> dao;

    public HechoRepository() {
        this(new HashMap<>());
    }

    public HechoRepository(Map<String, Object> daoConfig) {
        Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);

        // Configurar valores por defecto desde variables de entorno
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

        this.dao = DAOFactory.createDAO(Hecho.class, "hibernate", hibernateConfig);
    }

    public List<Hecho> find() {
        // Solo devolver hechos ACTIVOS
        return dao.find().stream()
            .filter(hecho -> hecho.getEstado() == EstadoHecho.ACTIVO)
            .collect(Collectors.toList());
    }

    public List<Hecho> findAll() {
        // Devolver todos los hechos sin filtrar (para uso interno)
        return dao.find();
    }

    public void save(Hecho hecho) {
        dao.save(hecho);
    }

    public void saveAll(List<Hecho> hechos) {
        dao.saveAll(hechos);
    }

    public Hecho findById(String uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;
            return hibernateDAO.findById(uuid);
        }
        return null;
    }

    public Contribuyente findContribuyenteByUserId(String userId) {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;
            return hibernateDAO.findContribuyenteByUserId(userId);
        }
        return null;
    }

    /**
     * Busca un hecho duplicado basado en título, ubicación y fecha
     * usando la misma lógica que EstrategiaTituloUbicacionFecha
     */
    public Hecho findDuplicado(String titulo, Double latitud, Double longitud, java.time.LocalDate fechaAcontecimiento) {
        if (!(dao instanceof Hibernate)) {
            return null;
        }

        Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;

        return hibernateDAO.executeQuery(em -> {
            // Normalizar título (trim + lowercase) como hace la estrategia
            String tituloNormalizado = titulo != null ? titulo.trim().toLowerCase() : null;

            // Tolerancia de ubicación: 0.0001 (aproximadamente 10 metros)
            double tolerancia = 0.0001;

            String jpql = "SELECT h FROM Hecho h WHERE " +
                         "LOWER(TRIM(h.titulo)) = :titulo AND " +
                         "ABS(h.latitud - :latitud) <= :tolerancia AND " +
                         "ABS(h.longitud - :longitud) <= :tolerancia AND " +
                         "h.fechaAcontecimiento = :fecha";

            List<Hecho> resultados = em.createQuery(jpql, Hecho.class)
                .setParameter("titulo", tituloNormalizado)
                .setParameter("latitud", latitud)
                .setParameter("longitud", longitud)
                .setParameter("tolerancia", tolerancia)
                .setParameter("fecha", fechaAcontecimiento)
                .setMaxResults(1)
                .getResultList();

            return resultados.isEmpty() ? null : resultados.get(0);
        });
    }

    /**
     * Obtiene contribuyentes por múltiples userIds en una sola query
     */
    public Map<String, Contribuyente> findContribuyentesByUserIds(java.util.Set<String> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }

        if (!(dao instanceof Hibernate)) {
            return new HashMap<>();
        }

        Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;

        return hibernateDAO.executeQuery(em -> {
            String jpql = "SELECT DISTINCT h.contribuyente FROM Hecho h WHERE h.contribuyente.userId IN :userIds";

            List<Contribuyente> contribuyentes = em.createQuery(jpql, Contribuyente.class)
                .setParameter("userIds", userIds)
                .getResultList();

            Map<String, Contribuyente> mapa = new HashMap<>();
            for (Contribuyente c : contribuyentes) {
                mapa.put(c.getUserId(), c);
            }
            return mapa;
        });
    }

    public RespuestaPaginadaDTO<Hecho> obtenerConFiltros(List<HechoStrategy> strategies, int pagina, int tamanioPagina) {
        if (strategies == null || strategies.isEmpty()) {
            return obtenerPaginados(pagina, tamanioPagina);
        }

        if (!(dao instanceof Hibernate)) {
            throw new UnsupportedOperationException("El filtrado de hechos requiere un DAO de tipo Hibernate");
        }

        if (!StrategyToSQLAdapter.todosSoportadosPorSQL(strategies)) {
            throw new UnsupportedOperationException("Uno o más filtros no son soportados por SQL");
        }

        return obtenerConFiltrosSQL(strategies, pagina, tamanioPagina);
    }

    private RespuestaPaginadaDTO<Hecho> obtenerConFiltrosSQL(List<HechoStrategy> strategies, int pagina, int tamanioPagina) {
        Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;

        return hibernateDAO.executeQuery(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            // Query para contar total
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Hecho> countRoot = countQuery.from(Hecho.class);
            List<Predicate> countPredicates = StrategyToSQLAdapter.convertirStrategiasASQL(strategies, cb, countRoot);

            if (!countPredicates.isEmpty()) {
                countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
            }
            countQuery.select(cb.count(countRoot));

            Long totalElementos = em.createQuery(countQuery).getSingleResult();

            // Query para obtener datos paginados
            CriteriaQuery<Hecho> dataQuery = cb.createQuery(Hecho.class);
            Root<Hecho> dataRoot = dataQuery.from(Hecho.class);
            List<Predicate> dataPredicates = StrategyToSQLAdapter.convertirStrategiasASQL(strategies, cb, dataRoot);

            if (!dataPredicates.isEmpty()) {
                dataQuery.where(cb.and(dataPredicates.toArray(new Predicate[0])));
            }

            dataQuery.select(dataRoot);

            TypedQuery<Hecho> typedQuery = em.createQuery(dataQuery);
            typedQuery.setFirstResult(pagina * tamanioPagina);
            typedQuery.setMaxResults(tamanioPagina);

            List<Hecho> hechos = typedQuery.getResultList();

            return new RespuestaPaginadaDTO<>(hechos, pagina, tamanioPagina, totalElementos);
        });
    }

    private RespuestaPaginadaDTO<Hecho> obtenerPaginados(int pagina, int tamanioPagina) {
        if (!(dao instanceof Hibernate)) {
            throw new UnsupportedOperationException("La paginación de hechos requiere un DAO de tipo Hibernate");
        }

        Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;

        return hibernateDAO.executeQuery(em -> {
            // Contar total
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<Hecho> countRoot = countQuery.from(Hecho.class);
            countQuery.select(cb.count(countRoot));
            Long totalElementos = em.createQuery(countQuery).getSingleResult();

            // Obtener datos paginados
            CriteriaQuery<Hecho> dataQuery = cb.createQuery(Hecho.class);
            Root<Hecho> dataRoot = dataQuery.from(Hecho.class);
            dataQuery.select(dataRoot);

            TypedQuery<Hecho> typedQuery = em.createQuery(dataQuery);
            typedQuery.setFirstResult(pagina * tamanioPagina);
            typedQuery.setMaxResults(tamanioPagina);

            List<Hecho> hechos = typedQuery.getResultList();

            return new RespuestaPaginadaDTO<>(hechos, pagina, tamanioPagina, totalElementos);
        });
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;
            hibernateDAO.close();
        }
    }
}
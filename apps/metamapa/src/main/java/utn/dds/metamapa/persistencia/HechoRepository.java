package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.jpa.entities.HechoEntity;
import utn.dds.mappers.HechoMapper;
import utn.dds.dto.RespuestaPaginadaDTO;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.TypedQuery;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class HechoRepository {
    private IDAO<HechoEntity> dao;

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

        this.dao = DAOFactory.createDAO(HechoEntity.class, "hibernate", hibernateConfig);
    }

    public List<Hecho> obtenerTodos() {
        List<HechoEntity> entities = dao.find();
        return entities.stream()
                .map(HechoMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Hecho obtenerPorId(String uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) dao;
            HechoEntity entity = hibernateDAO.findById(uuid);
            return entity != null ? HechoMapper.toDomain(entity) : null;
        }
        return null;
    }

    public void guardar(Hecho hecho) {
        HechoEntity entity = HechoMapper.toEntity(hecho);
        dao.save(entity);
    }

    public void guardarTodos(List<Hecho> hechos) {
        List<HechoEntity> entities = hechos.stream()
                .map(HechoMapper::toEntity)
                .collect(Collectors.toList());
        dao.saveAll(entities);
    }

    public void cambiarEstado(String uuid, utn.dds.dominio.EstadoHecho nuevoEstado) {
        if (dao instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) dao;
            HechoEntity entity = hibernateDAO.findById(uuid);
            if (entity != null) {
                entity.setEstado(nuevoEstado);
                dao.save(entity);
            }
        }
    }

    public RespuestaPaginadaDTO<Hecho> obtenerConFiltros(List<HechoStrategy> strategies, int pagina, int tamanioPagina) {
        if (strategies == null || strategies.isEmpty()) {
            return obtenerPaginados(pagina, tamanioPagina);
        }

        if (dao instanceof Hibernate && StrategyToSQLAdapter.todosSoportadosPorSQL(strategies)) {
            return obtenerConFiltrosSQL(strategies, pagina, tamanioPagina);
        } else {
            return obtenerConFiltrosMemoria(strategies, pagina, tamanioPagina);
        }
    }

    private RespuestaPaginadaDTO<Hecho> obtenerConFiltrosSQL(List<HechoStrategy> strategies, int pagina, int tamanioPagina) {
        Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) dao;

        return hibernateDAO.executeQuery(em -> {
            CriteriaBuilder cb = em.getCriteriaBuilder();

            // Query para contar total
            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<HechoEntity> countRoot = countQuery.from(HechoEntity.class);
            List<Predicate> countPredicates = StrategyToSQLAdapter.convertirStrategiasASQL(strategies, cb, countRoot);

            if (!countPredicates.isEmpty()) {
                countQuery.where(cb.and(countPredicates.toArray(new Predicate[0])));
            }
            countQuery.select(cb.count(countRoot));

            Long totalElementos = em.createQuery(countQuery).getSingleResult();

            // Query para obtener datos paginados
            CriteriaQuery<HechoEntity> dataQuery = cb.createQuery(HechoEntity.class);
            Root<HechoEntity> dataRoot = dataQuery.from(HechoEntity.class);
            List<Predicate> dataPredicates = StrategyToSQLAdapter.convertirStrategiasASQL(strategies, cb, dataRoot);

            if (!dataPredicates.isEmpty()) {
                dataQuery.where(cb.and(dataPredicates.toArray(new Predicate[0])));
            }

            dataQuery.select(dataRoot);

            TypedQuery<HechoEntity> typedQuery = em.createQuery(dataQuery);
            typedQuery.setFirstResult(pagina * tamanioPagina);
            typedQuery.setMaxResults(tamanioPagina);

            List<HechoEntity> entities = typedQuery.getResultList();
            List<Hecho> hechos = entities.stream()
                    .map(HechoMapper::toDomain)
                    .collect(Collectors.toList());

            return new RespuestaPaginadaDTO<>(hechos, pagina, tamanioPagina, totalElementos);
        });
    }

    private RespuestaPaginadaDTO<Hecho> obtenerConFiltrosMemoria(List<HechoStrategy> strategies, int pagina, int tamanioPagina) {
        // Fallback: filtrado en memoria (implementación actual)
        List<Hecho> todosLosHechos = obtenerTodos();

        List<Hecho> hechosFiltrados = todosLosHechos.stream()
                .filter(hecho -> strategies.stream().allMatch(strategy -> strategy.cumple(hecho)))
                .collect(Collectors.toList());

        long totalElementos = hechosFiltrados.size();
        int inicio = pagina * tamanioPagina;
        int fin = Math.min(inicio + tamanioPagina, hechosFiltrados.size());

        List<Hecho> hechosPaginados = hechosFiltrados.subList(inicio, fin);

        return new RespuestaPaginadaDTO<>(hechosPaginados, pagina, tamanioPagina, totalElementos);
    }

    private RespuestaPaginadaDTO<Hecho> obtenerPaginados(int pagina, int tamanioPagina) {
        if (dao instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Contar total
                CriteriaBuilder cb = em.getCriteriaBuilder();
                CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
                Root<HechoEntity> countRoot = countQuery.from(HechoEntity.class);
                countQuery.select(cb.count(countRoot));
                Long totalElementos = em.createQuery(countQuery).getSingleResult();

                // Obtener datos paginados
                CriteriaQuery<HechoEntity> dataQuery = cb.createQuery(HechoEntity.class);
                Root<HechoEntity> dataRoot = dataQuery.from(HechoEntity.class);
                dataQuery.select(dataRoot);

                TypedQuery<HechoEntity> typedQuery = em.createQuery(dataQuery);
                typedQuery.setFirstResult(pagina * tamanioPagina);
                typedQuery.setMaxResults(tamanioPagina);

                List<HechoEntity> entities = typedQuery.getResultList();
                List<Hecho> hechos = entities.stream()
                        .map(HechoMapper::toDomain)
                        .collect(Collectors.toList());

                return new RespuestaPaginadaDTO<>(hechos, pagina, tamanioPagina, totalElementos);
            });
        }

        // Fallback para otros tipos de DAO
        List<Hecho> todosLosHechos = obtenerTodos();
        long totalElementos = todosLosHechos.size();
        int inicio = pagina * tamanioPagina;
        int fin = Math.min(inicio + tamanioPagina, todosLosHechos.size());
        List<Hecho> hechosPaginados = todosLosHechos.subList(inicio, fin);

        return new RespuestaPaginadaDTO<>(hechosPaginados, pagina, tamanioPagina, totalElementos);
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) dao;
            hibernateDAO.close();
        }
    }
}
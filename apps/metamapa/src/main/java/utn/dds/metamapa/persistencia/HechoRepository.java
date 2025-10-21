package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dto.HechoDTO;

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

    public List<Hecho> obtenerTodos() {
        return dao.find();
    }

    public Hecho obtenerPorId(String uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;
            return hibernateDAO.findById(uuid);
        }
        return null;
    }

    public void guardar(Hecho hecho) {
        dao.save(hecho);
    }

    public void guardarTodos(List<Hecho> hechos) {
        dao.saveAll(hechos);
    }

    public void cambiarEstado(String uuid, utn.dds.dominio.EstadoHecho nuevoEstado) {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;
            Hecho hecho = hibernateDAO.findById(uuid);
            if (hecho != null) {
                hecho.setEstado(nuevoEstado);
                dao.save(hecho);
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

        // Fallback para otros tipos de DAO
        List<Hecho> todosLosHechos = obtenerTodos();
        long totalElementos = todosLosHechos.size();
        int inicio = pagina * tamanioPagina;
        int fin = Math.min(inicio + tamanioPagina, todosLosHechos.size());
        List<Hecho> hechosPaginados = todosLosHechos.subList(inicio, fin);

        return new RespuestaPaginadaDTO<>(hechosPaginados, pagina, tamanioPagina, totalElementos);
    }

    public RespuestaPaginadaDTO<HechoDTO> buscarHechosEnColeccion(String coleccionHandle, List<HechoStrategy> filtros, int pagina, int tamanioPagina) {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;

            return hibernateDAO.executeQuery(em -> {
                // Obtener todos los hechos de la colección (sin paginación para filtrar en memoria)
                List<Hecho> todosLosHechos = em.createQuery(
                    "SELECT h FROM Hecho h " +
                    "JOIN Coleccion c ON h MEMBER OF c.hechos " +
                    "WHERE c.handle = :handle",
                    Hecho.class)
                    .setParameter("handle", coleccionHandle)
                    .getResultList();

                // Aplicar filtros si existen
                List<Hecho> hechosFiltrados;
                if (filtros == null || filtros.isEmpty()) {
                    hechosFiltrados = todosLosHechos;
                } else {
                    hechosFiltrados = todosLosHechos.stream()
                            .filter(hecho -> filtros.stream().allMatch(filtro -> filtro.cumple(hecho)))
                            .collect(Collectors.toList());
                }

                long totalElementos = hechosFiltrados.size();

                // Aplicar paginación en memoria
                int inicio = pagina * tamanioPagina;
                int fin = Math.min(inicio + tamanioPagina, hechosFiltrados.size());
                List<Hecho> hechosPaginados = inicio < hechosFiltrados.size() ?
                    hechosFiltrados.subList(inicio, fin) : List.of();

                // Convertir a DTO
                List<HechoDTO> hechosDTO = hechosPaginados.stream()
                        .map(HechoDTO::fromHecho)
                        .collect(Collectors.toList());

                return new RespuestaPaginadaDTO<>(hechosDTO, pagina, tamanioPagina, totalElementos);
            });
        }

        // Fallback
        return new RespuestaPaginadaDTO<>(List.of(), pagina, tamanioPagina, 0L);
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;
            hibernateDAO.close();
        }
    }
}
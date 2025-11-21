package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.agregador.dominio.AgregacionJob;
import utn.dds.agregador.dominio.EstadoJob;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class AgregacionJobRepository {

    private IDAO<AgregacionJob> dao;

    public AgregacionJobRepository() {
        this(new HashMap<>());
    }

    public AgregacionJobRepository(Map<String, Object> daoConfig) {
        Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);

        // Configurar valores por defecto desde variables de entorno
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

        this.dao = DAOFactory.createDAO(AgregacionJob.class, "hibernate", hibernateConfig);
    }

    public void guardar(AgregacionJob job) {
        dao.save(job);
    }

    public void actualizar(AgregacionJob job) {
        if (dao instanceof Hibernate) {
            Hibernate<AgregacionJob> hibernateDAO = (Hibernate<AgregacionJob>) dao;
            hibernateDAO.update(job);
        }
    }

    public AgregacionJob obtenerPorId(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<AgregacionJob> hibernateDAO = (Hibernate<AgregacionJob>) dao;
            return hibernateDAO.findById(id);
        }
        return null;
    }

    public List<AgregacionJob> obtenerTodos() {
        return dao.find();
    }

    public List<AgregacionJob> obtenerPorEstado(EstadoJob estado) {
        if (dao instanceof Hibernate) {
            Hibernate<AgregacionJob> hibernateDAO = (Hibernate<AgregacionJob>) dao;
            return hibernateDAO.executeQuery(em -> {
                String jpql = "SELECT j FROM AgregacionJob j WHERE j.estado = :estado ORDER BY j.fechaInicio DESC";
                return em.createQuery(jpql, AgregacionJob.class)
                    .setParameter("estado", estado)
                    .getResultList();
            });
        }
        return List.of();
    }

    public List<AgregacionJob> obtenerRecientes(int limit) {
        if (dao instanceof Hibernate) {
            Hibernate<AgregacionJob> hibernateDAO = (Hibernate<AgregacionJob>) dao;
            return hibernateDAO.executeQuery(em -> {
                String jpql = "SELECT j FROM AgregacionJob j ORDER BY j.fechaInicio DESC";
                return em.createQuery(jpql, AgregacionJob.class)
                    .setMaxResults(limit)
                    .getResultList();
            });
        }
        return List.of();
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<AgregacionJob> hibernateDAO = (Hibernate<AgregacionJob>) dao;
            hibernateDAO.close();
        }
    }
}

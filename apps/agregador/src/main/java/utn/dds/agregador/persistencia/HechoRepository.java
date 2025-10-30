package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Hecho;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) dao;
            hibernateDAO.close();
        }
    }
}
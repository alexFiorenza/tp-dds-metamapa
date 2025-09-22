package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Hecho;
import utn.dds.jpa.entities.HechoEntity;
import utn.dds.mappers.HechoMapper;
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
    
    public List<Hecho> find() {
        List<HechoEntity> entities = dao.find();
        return entities.stream()
                .map(HechoMapper::toDomain)
                .collect(Collectors.toList());
    }
    
    public void save(Hecho hecho) {
        HechoEntity entity = HechoMapper.toEntity(hecho);
        dao.save(entity);
    }
    
    public void saveAll(List<Hecho> hechos) {
        List<HechoEntity> entities = hechos.stream()
                .map(HechoMapper::toEntity)
                .collect(Collectors.toList());
        dao.saveAll(entities);
    }

    public Hecho findById(String uuid) {
        if (dao instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) dao;
            HechoEntity entity = hibernateDAO.findById(uuid);
            return entity != null ? HechoMapper.toDomain(entity) : null;
        }
        return null;
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) dao;
            hibernateDAO.close();
        }
    }
}
package utn.dds.metamapa.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.Hibernate;
import utn.dds.dominio.Coleccion;
import utn.dds.jpa.entities.ColeccionEntity;
import utn.dds.mappers.ColeccionMapper;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class ColeccionRepository {
    private IDAO<ColeccionEntity> dao;

    public ColeccionRepository() {
        this(new HashMap<>());
    }

    public ColeccionRepository(Map<String, Object> daoConfig) {
        Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);

        // Configurar valores por defecto desde variables de entorno
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

        this.dao = DAOFactory.createDAO(ColeccionEntity.class, "hibernate", hibernateConfig);
    }

    public List<Coleccion> obtenerTodas() {
        List<ColeccionEntity> entities = dao.find();
        return entities.stream()
                .map(ColeccionMapper::toDomain)
                .collect(Collectors.toList());
    }

    public Coleccion obtenerPorId(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            ColeccionEntity entity = hibernateDAO.findById(id);
            return entity != null ? ColeccionMapper.toDomain(entity) : null;
        }
        return null;
    }

    public void crear(Coleccion coleccion) {
        ColeccionEntity entity = ColeccionMapper.toEntity(coleccion);
        dao.save(entity);
    }

    public void actualizar(String id, Coleccion coleccionActualizada) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            ColeccionEntity entity = hibernateDAO.findById(id);
            if (entity != null) {
                entity.setTitulo(coleccionActualizada.getTitulo());
                entity.setDescripcion(coleccionActualizada.getDescripcion());
                dao.save(entity);
            }
        }
    }

    public void eliminar(String id) {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            ColeccionEntity entity = hibernateDAO.findById(id);
            if (entity != null) {
                hibernateDAO.delete(entity);
            }
        }
    }

    public void close() {
        if (dao instanceof Hibernate) {
            Hibernate<ColeccionEntity> hibernateDAO = (Hibernate<ColeccionEntity>) dao;
            hibernateDAO.close();
        }
    }
}
package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.HibernateDAO;
import utn.dds.dto.HechoDTO;
import utn.dds.dominio.Hecho;
import utn.dds.jpa.entities.HechoEntity;
import utn.dds.mappers.HechoMapper;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

public class HechoRepository {

    private HibernateDAO<HechoEntity> hibernateDAO;  // DAO para entidades JPA
    private IDAO<HechoDTO> dao;    // DAO para DTOs (filesystem y s3)
    private String daoType;
    
    public HechoRepository() {
        // Constructor por defecto que usa configuración por defecto
        this("filesystem", new HashMap<>());
    }
    
    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
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

            this.hibernateDAO = new HibernateDAO<>(HechoEntity.class, hibernateConfig);
            this.dao = null;
        } else if ("filesystem".equals(daoType)) {
            // Para filesystem, usar configuración específica
            Map<String, Object> config = new HashMap<>();
            config.put("url", "src/main/resources/mocks/hechos.json");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, config);
            this.hibernateDAO = null;
        } else {
            // Para otros tipos de DAO, usar la configuración provista
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, daoConfig);
            this.hibernateDAO = null;
        }
    }
    
    public List<Hecho> find() {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, usar entidades JPA y mappers
            List<HechoEntity> entities = hibernateDAO.find();
            return entities.stream()
                    .map(HechoMapper::toDomain)
                    .collect(Collectors.toList());
        } else {
            // Para otros DAOs, usar DTOs
            List<HechoDTO> hechosDTO = dao.find();
            return hechosDTO.stream()
                    .map(HechoDTO::toHecho)
                    .collect(Collectors.toList());
        }
    }
    
    public void save(Hecho hecho) {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, convertir a entidad JPA y guardar
            HechoEntity entity = HechoMapper.toEntity(hecho);
            hibernateDAO.save(entity);
        } else {
            // Para otros DAOs, convertir a DTO y manejar como lista
            List<HechoDTO> hechosDTO = dao.find();
            hechosDTO.add(HechoDTO.fromHecho(hecho));
            dao.saveAll(hechosDTO);
        }
    }
    
    public void saveAll(List<Hecho> hechos) {
        if ("hibernate".equals(daoType)) {
            // Para Hibernate, convertir a entidades JPA y guardar
            List<HechoEntity> entities = hechos.stream()
                    .map(HechoMapper::toEntity)
                    .collect(Collectors.toList());
            hibernateDAO.saveAll(entities);
        } else {
            // Para otros DAOs, obtener existentes y agregar los nuevos
            List<HechoDTO> hechosDTO = dao.find();
            List<HechoDTO> nuevosHechosDTO = hechos.stream()
                    .map(HechoDTO::fromHecho)
                    .collect(Collectors.toList());
            hechosDTO.addAll(nuevosHechosDTO);
            dao.saveAll(hechosDTO);
        }
    }

    public Hecho findById(String uuid) {
        if ("hibernate".equals(daoType)) {
            HechoEntity entity = hibernateDAO.findById(uuid);
            return HechoMapper.toDomain(entity);
        } else {
            // Para otros DAOs, buscar en la lista
            List<HechoDTO> hechosDTO = dao.find();
            return hechosDTO.stream()
                    .filter(dto -> uuid.equals(dto.getUuid()))
                    .map(HechoDTO::toHecho)
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
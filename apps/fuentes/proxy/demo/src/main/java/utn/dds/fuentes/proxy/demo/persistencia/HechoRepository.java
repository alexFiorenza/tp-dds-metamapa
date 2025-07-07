package utn.dds.fuentes.proxy.demo.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;
import utn.dds.dominio.Hecho;
import java.util.List;
import java.util.Map;

public class HechoRepository {
    private final IDAO<Hecho> dao;
    
    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
        this.dao = DAOFactory.createDAO(Hecho.class, daoType, daoConfig);
    }
    
    public HechoRepository(IDAO<Hecho> dao) {
        this.dao = dao;
    }

    public List<Hecho> obtener() {
        return dao.find();
    }

    public List<Hecho> actualizar(List<Hecho> hechos){
        // Agrego los nuevos hechos a la cache
        dao.addAll(hechos);
        // Devuelvo todos los hechos actualizados
        return this.obtener();
    }
}
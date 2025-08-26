package utn.dds.fuentes.estatica.persistencia;

import utn.dds.dominio.Hecho;
import utn.dds.daos.IDAO;
import utn.dds.daos.DAOFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HechoRepository {
    private final IDAO<Hecho> dao;

    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
        this.dao = DAOFactory.createDAO(Hecho.class, daoType, daoConfig);
    }
    
    public HechoRepository(IDAO<Hecho> dao) {
        this.dao = dao;
    }

    public InputStream leerArchivo() throws IOException {
        return dao.read();
    }

    public InputStream leerArchivo(String path) throws IOException {
        return dao.read(path);
    }
}
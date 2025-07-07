package utn.dds.fuentes.estatica.persistencia;

import utn.dds.dominio.Hecho;
import utn.dds.daos.IDAO;

import java.io.IOException;
import java.io.InputStream;

public class HechoRepository {
    private final IDAO<Hecho> dao;

    public HechoRepository(IDAO<Hecho> dao) {
        this.dao = dao;
    }

    public InputStream leerArchivo() throws IOException {
        return dao.read();
    }
}
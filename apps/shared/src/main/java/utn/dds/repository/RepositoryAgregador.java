package utn.dds.repository;

import utn.dds.dominio.Hecho;
import java.util.List;

public class RepositoryAgregador {
    private IDAO<Hecho> dao;

    public RepositoryAgregador(IDAO<Hecho> dao) {
        this.dao = dao;
    }

    public List<Hecho> find() {
        // TODO: Implementar lógica de agregación
        return dao.find();
    }

    public void save(List<Hecho> hechos) {
        // TODO: Implementar lógica de guardado
        for (Hecho hecho : hechos) {
            dao.save(hecho);
        }
    }
} 
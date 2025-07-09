package utn.dds.metamapa;

import utn.dds.dominio.Hecho;

import java.util.List;

public class ServiceHecho {
    private final RepositoryHechos repositoryHecho;
    public ServiceHecho(){
        this.repositoryHecho=new RepositoryHechos();
    }
    public List<Hecho> buscarHechos() {
        return repositoryHecho.obtenerHechos();
    }

}

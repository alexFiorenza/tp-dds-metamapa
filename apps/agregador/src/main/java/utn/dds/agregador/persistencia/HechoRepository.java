package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;

public class HechoRepository implements IDAO<Hecho> {
    
    private List<Hecho> hechos;
    
    public HechoRepository() {
        this.hechos = new ArrayList<>();
    }
    
    @Override
    public InputStream read() {
        return null;
    }
    
    @Override
    public InputStream read(String path) {
        return null;
    }
    
    @Override
    public List<Hecho> find() {
        return new ArrayList<>(hechos);
    }
    
    @Override
    public void save(Hecho hecho) {
        hechos.add(hecho);
    }
    
    @Override
    public void saveAll(List<Hecho> hechos) {
        for (Hecho hecho : hechos) {
            save(hecho);
        }
    }
    
    @Override
    public void addAll(List<Hecho> hechos) {
        saveAll(hechos);
    }
}
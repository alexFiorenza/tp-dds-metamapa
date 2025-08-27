package utn.dds.agregador.persistencia;

import utn.dds.daos.IDAO;
import utn.dds.dto.FuenteDTO;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.io.InputStream;

public class FuentesRepository implements IDAO<FuenteDTO> {
    
    private List<FuenteDTO> fuentes;
    
    public FuentesRepository() {
        this.fuentes = new ArrayList<>();
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
    public List<FuenteDTO> find() {
        return new ArrayList<>(fuentes);
    }
    
    @Override
    public void save(FuenteDTO fuente) {
        if (fuente.getUuid() == null) {
            fuente.setUuid(UUID.randomUUID());
        }
        fuentes.add(fuente);
    }
    
    @Override
    public void saveAll(List<FuenteDTO> fuentes) {
        for (FuenteDTO fuente : fuentes) {
            save(fuente);
        }
    }
    
    @Override
    public void addAll(List<FuenteDTO> fuentes) {
        saveAll(fuentes);
    }
    
    public FuenteDTO findByUrl(String url) {
        return fuentes.stream()
                .filter(f -> f.getUrl().equals(url))
                .findFirst()
                .orElse(null);
    }
    
    public boolean removeByUrl(String url) {
        return fuentes.removeIf(f -> f.getUrl().equals(url));
    }
}
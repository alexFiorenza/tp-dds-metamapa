package utn.dds.agregador.service;

import utn.dds.agregador.persistencia.FuentesRepository;
import utn.dds.dto.FuenteDTO;
import java.util.List;
import java.util.UUID;

public class ServiceRegistry {
    
    private FuentesRepository fuentesRepository;
    
    public ServiceRegistry(FuentesRepository fuentesRepository) {
        this.fuentesRepository = fuentesRepository;
    }
    
    public void registrar(FuenteDTO fuente) {
        if (fuente.getUrl() == null || fuente.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("La URL de la fuente no puede estar vacía");
        }
        
        FuenteDTO existente = fuentesRepository.findByUrl(fuente.getUrl());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una fuente registrada con esa URL");
        }
        
        if (fuente.getUuid() == null) {
            fuente.setUuid(UUID.randomUUID());
        }
        
        fuentesRepository.save(fuente);
    }
    
    public List<FuenteDTO> obtenerTodasLasFuentes() {
        return fuentesRepository.find();
    }
    
    public FuenteDTO obtenerFuentePorUrl(String url) {
        return fuentesRepository.findByUrl(url);
    }
    
    public boolean eliminarFuente(String url) {
        return fuentesRepository.removeByUrl(url);
    }
}
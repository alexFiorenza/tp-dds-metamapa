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
        if (fuente.getHost() == null || fuente.getHost().trim().isEmpty()) {
            throw new IllegalArgumentException("El host de la fuente no puede estar vacío");
        }
        
        FuenteDTO existente = fuentesRepository.findByHost(fuente.getHost());
        if (existente != null) {
            throw new IllegalArgumentException("Ya existe una fuente registrada con ese host");
        }
        
        if (fuente.getUuid() == null) {
            fuente.setUuid(UUID.randomUUID());
        }
        
        fuentesRepository.save(fuente);
    }
    
    public List<FuenteDTO> obtenerTodasLasFuentes() {
        return fuentesRepository.find();
    }
    
    public FuenteDTO obtenerFuentePorHost(String host) {
        return fuentesRepository.findByHost(host);
    }
    
    public boolean eliminarFuente(String host) {
        return fuentesRepository.removeByHost(host);
    }
    
    public boolean eliminarFuentePorUuid(UUID uuid) {
        return fuentesRepository.removeByUuid(uuid);
    }
    
    public FuenteDTO obtenerFuentePorUuid(UUID uuid) {
        return fuentesRepository.findByUuid(uuid);
    }
}
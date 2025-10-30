package utn.dds.agregador.service;

import utn.dds.persistencia.FuentesRepository;
import utn.dds.dto.FuenteDTO;
import java.util.List;
import java.util.Map;
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
        
        // Verificar si ya existe una fuente con el mismo host y parámetros
        List<FuenteDTO> fuentesExistentes = fuentesRepository.find();
        boolean duplicado = fuentesExistentes.stream().anyMatch(f -> 
            f.getHost().equals(fuente.getHost()) && 
            sonParametrosIguales(f.getParams(), fuente.getParams())
        );
        
        if (duplicado) {
            throw new IllegalArgumentException("Ya existe una fuente registrada con el mismo host y parámetros");
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
    
    public List<FuenteDTO> obtenerFuentesPorHost(String host) {
        return fuentesRepository.findAllByHost(host);
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
    
    private boolean sonParametrosIguales(Map<String, Object> params1, Map<String, Object> params2) {
        // Si ambos son null, son iguales
        if (params1 == null && params2 == null) {
            return true;
        }
        
        // Si uno es null y el otro no, no son iguales
        if (params1 == null || params2 == null) {
            return false;
        }
        
        // Comparar usando equals del Map
        return params1.equals(params2);
    }
}
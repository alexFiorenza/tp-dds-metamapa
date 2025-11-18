package utn.dds.agregador.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.agregador.persistencia.HechoRepository;
import utn.dds.persistencia.FuentesRepository;
import utn.dds.dominio.Hecho;
import utn.dds.dto.FuenteDTO;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServiceRegistry {

    private static final Logger logger = LoggerFactory.getLogger(ServiceRegistry.class);

    private FuentesRepository fuentesRepository;
    private HechoRepository hechoRepository;

    public ServiceRegistry(FuentesRepository fuentesRepository) {
        this.fuentesRepository = fuentesRepository;
        this.hechoRepository = null; // Se inicializa después si es necesario
    }

    public ServiceRegistry(FuentesRepository fuentesRepository, HechoRepository hechoRepository) {
        this.fuentesRepository = fuentesRepository;
        this.hechoRepository = hechoRepository;
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

        // Inicializar metadata para fuentes estáticas si no está presente
        if ("estatica".equals(fuente.getTipo()) && fuente.getMetadata() == null) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("procesado", false);
            fuente.setMetadata(metadata);
        }

        fuentesRepository.save(fuente);
    }

    public void actualizar(FuenteDTO fuente) {
        if (fuente.getUuid() == null) {
            throw new IllegalArgumentException("El UUID es requerido para actualizar una fuente");
        }

        FuenteDTO fuenteExistente = fuentesRepository.findByUuid(fuente.getUuid());
        if (fuenteExistente == null) {
            throw new IllegalArgumentException("No existe una fuente con UUID: " + fuente.getUuid());
        }

        fuentesRepository.update(fuente);
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
        // Primero ocultar todos los hechos que tienen como origen esta fuente
        if (hechoRepository != null) {
            String uuidString = uuid.toString();
            List<Hecho> todosLosHechos = hechoRepository.findAll();

            int hechosOcultados = 0;
            for (Hecho hecho : todosLosHechos) {
                if (uuidString.equals(hecho.getOrigen())) {
                    hecho.ocultar();
                    hechoRepository.save(hecho);
                    hechosOcultados++;
                }
            }

            logger.info("Se ocultaron {} hechos de la fuente {}", hechosOcultados, uuid);
        }

        // Luego eliminar la fuente
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
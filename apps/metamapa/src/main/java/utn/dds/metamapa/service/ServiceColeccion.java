package utn.dds.metamapa.service;

import utn.dds.metamapa.persistencia.ColeccionRepository;
import utn.dds.metamapa.persistencia.HechoRepository;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.Fuente;
import utn.dds.dominio.Criterio;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.ColeccionCreateDTO;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.CriterioCreateDTO;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dominio.criterios.*;
import utn.dds.dominio.EstadoHecho;
import utn.dds.daos.Hibernate;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.UUID;

public class ServiceColeccion {
    private final ColeccionRepository coleccionRepository;
    private final HechoRepository hechoRepository;
    private final IDAO<Fuente> fuenteDAO;
    private final IDAO<Hecho> hechoDAO;

    public ServiceColeccion(String daoType, Map<String, Object> daoConfig) {
        this.coleccionRepository = new ColeccionRepository(daoConfig);
        this.hechoRepository = new HechoRepository(daoConfig);

        // Configurar DAO para fuentes
        Map<String, Object> hibernateConfig = new HashMap<>(daoConfig);
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.putIfAbsent("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.putIfAbsent("persistenceUnit", "metamapa-db");

        this.fuenteDAO = DAOFactory.createDAO(Fuente.class, "hibernate", hibernateConfig);
        this.hechoDAO = DAOFactory.createDAO(Hecho.class, "hibernate", hibernateConfig);
    }

    public RespuestaPaginadaDTO<ColeccionDTO> obtenerColecciones(int page, int size) {
        // Valores por defecto si los parámetros no son válidos
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10; // Máximo 100 elementos por página

        RespuestaPaginadaDTO<Coleccion> respuesta = this.coleccionRepository.obtenerTodas(page, size);

        // Mapear de entity a DTO
        List<ColeccionDTO> coleccionesDTO = respuesta.getDatos().stream()
                .map(ColeccionDTO::from)
                .collect(Collectors.toList());

        return new RespuestaPaginadaDTO<>(coleccionesDTO, page, size, respuesta.getTotalElementos());
    }

    public ColeccionDTO obtenerColeccionPorId(String id) {
        Coleccion entity = this.coleccionRepository.obtenerPorId(id);
        return entity != null ? ColeccionDTO.from(entity) : null;
    }

    public ColeccionDTO crearColeccion(ColeccionCreateDTO coleccionCreateDTO) {
        // Crear Coleccion directamente desde el DTO
        Coleccion coleccion = new Coleccion();
        coleccion.setTitulo(coleccionCreateDTO.getTitulo());
        coleccion.setDescripcion(coleccionCreateDTO.getDescripcion());

        // Convertir String a instancia de AlgoritmoConsenso usando el Factory
        String tipoAlgoritmo = coleccionCreateDTO.getAlgoritmoConsenso();
        coleccion.setAlgoritmoConsenso(
            utn.dds.dominio.consenso.AlgoritmoConsensoFactory.crear(tipoAlgoritmo)
        );

        // Convertir criterios de DTO a Criterio domain entities
        List<HechoStrategy> criteriosStrategy = new ArrayList<>();
        if (coleccionCreateDTO.getCriteriosDePertenencia() != null) {
            List<Criterio> criterios = new ArrayList<>();
            for (CriterioCreateDTO criterioDTO : coleccionCreateDTO.getCriteriosDePertenencia()) {
                HechoStrategy strategy = criterioDTO.toHechoStrategy();
                Criterio criterio = Criterio.fromHechoStrategy(strategy, coleccion.getHandle());
                criterios.add(criterio);
                criteriosStrategy.add(strategy);
            }
            coleccion.setCriteriosDePertenencia(criterios);
        }

        // 1. Buscar las fuentes por sus IDs
        List<Fuente> fuentes = buscarFuentesPorIds(coleccionCreateDTO.getFuentesIds());
        coleccion.setFuentes(fuentes);

        // 2. Inicializar lista vacía de hechos (se poblará después)
        coleccion.setHechos(new ArrayList<>());

        // 3. Guardar la colección SIN hechos (mucho más rápido)
        guardarColeccionConEntidades(coleccion);

        // 4. Asociar hechos directamente en la BD (evita cargar en memoria)
        this.coleccionRepository.asociarHechosDesdeFuentes(
            coleccion.getHandle(),
            coleccionCreateDTO.getFuentesIds(),
            criteriosStrategy
        );

        // 5. Retornar la colección creada como DTO
        return ColeccionDTO.from(coleccion);
    }

    private List<Fuente> buscarFuentesPorIds(List<String> fuentesIds) {
        if (fuentesIds == null || fuentesIds.isEmpty()) {
            return new ArrayList<>();
        }

        if (fuenteDAO instanceof Hibernate) {
            Hibernate<Fuente> hibernateDAO = (Hibernate<Fuente>) fuenteDAO;
            return hibernateDAO.executeQuery(em -> {
                return em.createQuery("SELECT f FROM Fuente f WHERE f.uuid IN :ids", Fuente.class)
                        .setParameter("ids", fuentesIds)
                        .getResultList();
            });
        }
        return new ArrayList<>();
    }


    private void guardarColeccionConEntidades(Coleccion coleccion) {
        // Crear un DAO temporal para guardar la entidad directamente
        Map<String, Object> hibernateConfig = new HashMap<>();
        hibernateConfig.put("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.put("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.put("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.put("persistenceUnit", "metamapa-db");

        IDAO<Coleccion> coleccionDAO =
            DAOFactory.createDAO(Coleccion.class, "hibernate", hibernateConfig);

        coleccionDAO.save(coleccion);
    }

    public ColeccionDTO actualizarColeccion(String id, utn.dds.dto.ColeccionUpdateDTO updateDTO) {
        // Verificar que la colección existe (entity is already domain)
        Coleccion entity = this.coleccionRepository.obtenerPorId(id);
        if (entity == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        Coleccion coleccionExistente = entity;

        boolean actualizarCriterios = updateDTO.getCriteriosDePertenencia() != null;
        boolean actualizarFuentes = updateDTO.getFuentesIds() != null;
        boolean actualizarCamposBasicos = updateDTO.getTitulo() != null || updateDTO.getDescripcion() != null;

        // Convertir criterios de DTO a domain
        List<HechoStrategy> nuevosCriterios = null;
        if (actualizarCriterios) {
            nuevosCriterios = convertirCriteriosDTO(updateDTO.getCriteriosDePertenencia());
        }

        if (actualizarCriterios && actualizarFuentes) {
            // Caso 1: Actualizar criterios Y fuentes
            actualizarCriteriosYFuentes(id, nuevosCriterios, updateDTO.getFuentesIds(), coleccionExistente);
        } else if (actualizarCriterios) {
            // Caso 2: Solo actualizar criterios
            actualizarSoloCriterios(id, nuevosCriterios, coleccionExistente);
        } else if (actualizarFuentes) {
            // Caso 3: Solo actualizar fuentes
            actualizarSoloFuentes(id, updateDTO.getFuentesIds(), coleccionExistente);
        }

        // Caso 4: Actualizar campos básicos (siempre al final)
        if (actualizarCamposBasicos) {
            this.coleccionRepository.actualizarCamposBasicos(id, updateDTO.getTitulo(), updateDTO.getDescripcion());
        }

        // Obtener la colección actualizada y retornarla como DTO
        Coleccion coleccionActualizada = this.coleccionRepository.obtenerPorId(id);
        return ColeccionDTO.from(coleccionActualizada);
    }

    private void actualizarCriteriosYFuentes(String id, List<HechoStrategy> nuevosCriterios,
                                           List<String> nuevasFuentesIds, Coleccion coleccionExistente) {
        // 1. Actualizar criterios primero en la BD
        this.coleccionRepository.actualizarCriterios(id, nuevosCriterios);

        // 2. Buscar las nuevas fuentes por sus IDs
        List<Fuente> nuevasFuentes = buscarFuentesPorIds(nuevasFuentesIds);

        // 3. Actualizar fuentes
        this.coleccionRepository.actualizarFuentes(id, nuevasFuentes);

        // 4. Limpiar hechos existentes y re-asociar con nuevos criterios
        this.coleccionRepository.actualizarHechos(id, new ArrayList<>());

        // 5. Asociar hechos directamente en BD (optimizado)
        this.coleccionRepository.asociarHechosDesdeFuentes(id, nuevasFuentesIds, nuevosCriterios);
    }

    private void actualizarSoloCriterios(String id, List<HechoStrategy> nuevosCriterios, Coleccion coleccionExistente) {
        // 1. Actualizar criterios en la BD
        this.coleccionRepository.actualizarCriterios(id, nuevosCriterios);

        // 2. Limpiar hechos existentes
        this.coleccionRepository.actualizarHechos(id, new ArrayList<>());

        // 3. Re-asociar hechos con nuevos criterios (optimizado)
        List<String> fuentesIds = coleccionExistente.getFuentes().stream()
            .map(Fuente::getUuid)
            .collect(Collectors.toList());

        this.coleccionRepository.asociarHechosDesdeFuentes(id, fuentesIds, nuevosCriterios);
    }

    private void actualizarSoloFuentes(String id, List<String> nuevasFuentesIds, Coleccion coleccionExistente) {
        // 1. Buscar las nuevas fuentes por sus IDs
        List<Fuente> nuevasFuentes = buscarFuentesPorIds(nuevasFuentesIds);

        // 2. Actualizar fuentes
        this.coleccionRepository.actualizarFuentes(id, nuevasFuentes);

        // 3. Limpiar hechos existentes
        this.coleccionRepository.actualizarHechos(id, new ArrayList<>());

        // 4. Re-asociar hechos con las nuevas fuentes (optimizado)
        List<HechoStrategy> criteriosExistentes = coleccionExistente.getCriteriosDePertenenciaAsStrategies();
        this.coleccionRepository.asociarHechosDesdeFuentes(id, nuevasFuentesIds, criteriosExistentes);
    }

    private List<HechoStrategy> convertirCriteriosDTO(List<utn.dds.dto.CriterioCreateDTO> criteriosDTO) {
        if (criteriosDTO == null || criteriosDTO.isEmpty()) {
            return new ArrayList<>();
        }

        return criteriosDTO.stream()
            .map(criterio -> {
                try {
                    return criterio.toHechoStrategy();
                } catch (Exception e) {
                    // Si hay error al convertir, ignorar este criterio
                    return null;
                }
            })
            .filter(criterio -> criterio != null)
            .collect(Collectors.toList());
    }

    public void eliminarColeccion(String id) {
        Coleccion entity = this.coleccionRepository.obtenerPorId(id);
        if (entity == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        this.coleccionRepository.eliminar(id);
    }

    public RespuestaPaginadaDTO<HechoDTO> obtenerHechosDeColeccion(String handle, List<HechoStrategy> filtros, int page, int size, String modo) {
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        // Verificar que la colección existe
        Coleccion entity = this.coleccionRepository.obtenerPorId(handle);
        if (entity == null) {
            throw new RuntimeException("Colección no encontrada");
        }

        // Si el modo es "irrestricto", retornar todos los hechos sin aplicar consenso
        if (modo != null && modo.equalsIgnoreCase("irrestricto")) {
            return this.hechoRepository.buscarHechosEnColeccion(handle, filtros, page, size);
        }

        // Si el modo es "curado" o no se especifica, aplicar algoritmo de consenso
        // Primero obtener todos los hechos de la colección (sin paginar)
        List<Hecho> todosLosHechos = this.coleccionRepository.obtenerHechos(handle);
        
        // Aplicar algoritmo de consenso si existe
        List<Hecho> hechosConsensuados;
        if (entity.getAlgoritmoConsenso() != null && entity.getFuentes() != null) {
            hechosConsensuados = entity.getAlgoritmoConsenso()
                .filtrarHechosConsensuados(todosLosHechos, entity.getFuentes());
        } else {
            // Si no hay algoritmo, todos los hechos son consensuados
            hechosConsensuados = todosLosHechos;
        }

        // Aplicar filtros adicionales si existen
        if (filtros != null && !filtros.isEmpty()) {
            hechosConsensuados = hechosConsensuados.stream()
                .filter(hecho -> filtros.stream().allMatch(filtro -> filtro.cumple(hecho)))
                .collect(Collectors.toList());
        }

        // Convertir a DTO
        List<HechoDTO> hechosDTO = hechosConsensuados.stream()
            .map(HechoDTO::fromHecho)
            .collect(Collectors.toList());

        // Aplicar paginación manual
        int totalElementos = hechosDTO.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, totalElementos);
        List<HechoDTO> hechosPaginados = fromIndex < totalElementos ? 
            hechosDTO.subList(fromIndex, toIndex) : new ArrayList<>();

        return new RespuestaPaginadaDTO<>(hechosPaginados, page, size, totalElementos);
    }
}

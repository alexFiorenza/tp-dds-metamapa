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

        // Convertir criterios de DTO a Criterio domain entities
        if (coleccionCreateDTO.getCriteriosDePertenencia() != null) {
            List<Criterio> criterios = new ArrayList<>();
            for (CriterioCreateDTO criterioDTO : coleccionCreateDTO.getCriteriosDePertenencia()) {
                HechoStrategy strategy = criterioDTO.toHechoStrategy();
                Criterio criterio = Criterio.fromHechoStrategy(strategy, coleccion.getHandle());
                criterios.add(criterio);
            }
            coleccion.setCriteriosDePertenencia(criterios);
        }

        // 1. Buscar las fuentes por sus IDs
        List<Fuente> fuentes = buscarFuentesPorIds(coleccionCreateDTO.getFuentesIds());

        // 2. Buscar hechos que tengan origen en alguna de estas fuentes
        List<Hecho> hechosDeEstasFuentes = buscarHechosPorOrigenFuentes(fuentes);

        // 3. Aplicar criterios de pertenencia para filtrar los hechos
        List<HechoStrategy> criterios = coleccion.getCriteriosDePertenenciaAsStrategies();
        List<Hecho> hechosFiltrados = aplicarCriterios(hechosDeEstasFuentes, criterios);

        // 4. Establecer hechos y fuentes en la colección
        coleccion.setHechos(hechosFiltrados);
        coleccion.setFuentes(fuentes);

        // 5. Buscar las Hecho entities existentes por sus UUIDs
        List<String> hechosIds = hechosFiltrados.stream()
                .map(Hecho::getUuid)
                .collect(Collectors.toList());
        List<Hecho> hechoEntities = buscarHechoEntitiesPorIds(hechosIds);
        coleccion.setHechos(hechoEntities);

        // 6. Guardar la colección directamente (con las relaciones ManyToMany)
        guardarColeccionConEntidades(coleccion);

        // 7. Retornar la colección creada como DTO
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

    private List<Hecho> buscarHechoEntitiesPorIds(List<String> hechosIds) {
        if (hechosIds == null || hechosIds.isEmpty()) {
            return new ArrayList<>();
        }

        if (hechoDAO instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) hechoDAO;
            return hibernateDAO.executeQuery(em -> {
                return em.createQuery("SELECT h FROM Hecho h WHERE h.uuid IN :ids", Hecho.class)
                        .setParameter("ids", hechosIds)
                        .getResultList();
            });
        }
        return new ArrayList<>();
    }

    private List<Hecho> buscarHechosPorOrigenFuentes(List<Fuente> fuentes) {
        if (fuentes == null || fuentes.isEmpty()) {
            return new ArrayList<>();
        }

        // Extraer los UUIDs de las fuentes para buscar hechos con ese origen
        List<String> origenes = fuentes.stream()
                .map(Fuente::getUuid)
                .collect(Collectors.toList());

        // Buscar todos los hechos que tengan origen en alguna de estas fuentes
        List<Hecho> todosLosHechos = hechoRepository.obtenerTodos();
        return todosLosHechos.stream()
                .filter(hecho -> origenes.contains(hecho.getOrigen()))
                .collect(Collectors.toList());
    }

    private List<Hecho> aplicarCriterios(List<Hecho> hechos, List<HechoStrategy> criterios) {
        if (criterios == null || criterios.isEmpty()) {
            return hechos;
        }

        return hechos.stream()
                .filter(hecho -> criterios.stream().allMatch(criterio -> criterio.cumple(hecho)))
                .collect(Collectors.toList());
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

        // 3. Buscar hechos de las nuevas fuentes
        List<Hecho> hechosNuevasFuentes = buscarHechosPorOrigenFuentes(nuevasFuentes);

        // 4. Obtener hechos existentes de la colección actual
        List<Hecho> hechosExistentes = coleccionExistente.getHechos();

        // 5. Combinar hechos existentes + nuevos
        List<Hecho> todosLosHechos = new ArrayList<>(hechosExistentes);
        todosLosHechos.addAll(hechosNuevasFuentes);

        // 6. Aplicar criterios a todos los hechos
        List<Hecho> hechosFiltrados = aplicarCriterios(todosLosHechos, nuevosCriterios);

        // 7. Actualizar fuentes y hechos en la BD
        this.coleccionRepository.actualizarFuentes(id, nuevasFuentes);

        List<Hecho> hechoEntities = hechosFiltrados.stream()
            .map(hecho -> buscarHechoEntityPorId(hecho.getUuid()))
            .filter(entity -> entity != null)
            .collect(Collectors.toList());

        this.coleccionRepository.actualizarHechos(id, hechoEntities);
    }

    private void actualizarSoloCriterios(String id, List<HechoStrategy> nuevosCriterios, Coleccion coleccionExistente) {
        // 1. Actualizar criterios en la BD
        this.coleccionRepository.actualizarCriterios(id, nuevosCriterios);

        // 2. Obtener hechos existentes
        List<Hecho> hechosExistentes = coleccionExistente.getHechos();

        // 3. Aplicar nuevos criterios a hechos existentes
        List<Hecho> hechosFiltrados = aplicarCriterios(hechosExistentes, nuevosCriterios);

        // 4. Actualizar hechos filtrados
        List<Hecho> hechoEntities = hechosFiltrados.stream()
            .map(hecho -> buscarHechoEntityPorId(hecho.getUuid()))
            .filter(entity -> entity != null)
            .collect(Collectors.toList());

        this.coleccionRepository.actualizarHechos(id, hechoEntities);
    }

    private void actualizarSoloFuentes(String id, List<String> nuevasFuentesIds, Coleccion coleccionExistente) {
        // 1. Buscar las nuevas fuentes por sus IDs
        List<Fuente> nuevasFuentes = buscarFuentesPorIds(nuevasFuentesIds);

        // 2. Buscar hechos de las nuevas fuentes
        List<Hecho> hechosNuevasFuentes = buscarHechosPorOrigenFuentes(nuevasFuentes);

        // 3. Obtener hechos existentes de la colección actual
        List<Hecho> hechosExistentes = coleccionExistente.getHechos();

        // 4. Combinar hechos existentes + nuevos
        List<Hecho> todosLosHechos = new ArrayList<>(hechosExistentes);
        todosLosHechos.addAll(hechosNuevasFuentes);

        // 5. Aplicar criterios existentes si los hay
        List<HechoStrategy> criteriosExistentes = coleccionExistente.getCriteriosDePertenenciaAsStrategies();
        List<Hecho> hechosFiltrados = aplicarCriterios(todosLosHechos, criteriosExistentes);

        // 6. Actualizar fuentes y hechos en la BD
        this.coleccionRepository.actualizarFuentes(id, nuevasFuentes);

        List<Hecho> hechoEntities = hechosFiltrados.stream()
            .map(hecho -> buscarHechoEntityPorId(hecho.getUuid()))
            .filter(entity -> entity != null)
            .collect(Collectors.toList());

        this.coleccionRepository.actualizarHechos(id, hechoEntities);
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

    private Hecho buscarHechoEntityPorId(String uuid) {
        if (hechoDAO instanceof Hibernate) {
            Hibernate<Hecho> hibernateDAO = (Hibernate<Hecho>) hechoDAO;
            return hibernateDAO.findById(uuid);
        }
        return null;
    }

    public void eliminarColeccion(String id) {
        Coleccion entity = this.coleccionRepository.obtenerPorId(id);
        if (entity == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        this.coleccionRepository.eliminar(id);
    }

    public RespuestaPaginadaDTO<HechoDTO> obtenerHechosDeColeccion(String handle, List<HechoStrategy> filtros, int page, int size) {
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        // Verificar que la colección existe
        Coleccion entity = this.coleccionRepository.obtenerPorId(handle);
        if (entity == null) {
            throw new RuntimeException("Colección no encontrada");
        }

        // Usar HechoRepository para consulta directa optimizada
        return this.hechoRepository.buscarHechosEnColeccion(handle, filtros, page, size);
    }
}

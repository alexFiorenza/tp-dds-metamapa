package utn.dds.metamapa.service;

import utn.dds.metamapa.persistencia.ColeccionRepository;
import utn.dds.metamapa.persistencia.HechoRepository;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.ColeccionCreateDTO;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.mappers.ColeccionMapper;
import utn.dds.dominio.criterios.*;
import utn.dds.dominio.EstadoHecho;
import utn.dds.jpa.entities.FuenteEntity;
import utn.dds.jpa.entities.HechoEntity;
import utn.dds.daos.Hibernate;
import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.Arrays;

public class ServiceColeccion {
    private final ColeccionRepository coleccionRepository;
    private final HechoRepository hechoRepository;
    private final IDAO<FuenteEntity> fuenteDAO;
    private final IDAO<HechoEntity> hechoDAO;

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

        this.fuenteDAO = DAOFactory.createDAO(FuenteEntity.class, "hibernate", hibernateConfig);
        this.hechoDAO = DAOFactory.createDAO(HechoEntity.class, "hibernate", hibernateConfig);
    }

    public List<Coleccion> obtenerColecciones() {
        return this.coleccionRepository.obtenerTodas();
    }

    public List<ColeccionDTO> obtenerColeccionesDTO() {
        return this.coleccionRepository.obtenerTodasDTO();
    }

    public RespuestaPaginadaDTO<ColeccionDTO> obtenerColeccionesPaginado(int page, int size) {
        // Valores por defecto si los parámetros no son válidos
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10; // Máximo 100 elementos por página

        return this.coleccionRepository.obtenerTodasDTOPaginado(page, size);
    }

    public Coleccion obtenerColeccionPorId(String id) {
        return this.coleccionRepository.obtenerPorId(id);
    }

    public ColeccionDTO obtenerColeccionDTOPorId(String id) {
        return this.coleccionRepository.obtenerDTOPorId(id);
    }

    public void crearColeccion(ColeccionCreateDTO coleccionCreateDTO) {
        Coleccion coleccion = ColeccionMapper.fromCreateDTO(coleccionCreateDTO);

        // 1. Buscar las fuentes por sus IDs
        List<FuenteEntity> fuentes = buscarFuentesPorIds(coleccionCreateDTO.getFuentesIds());

        // 2. Buscar hechos que tengan origen en alguna de estas fuentes
        List<Hecho> hechosDeEstasFuentes = buscarHechosPorOrigenFuentes(fuentes);

        // 3. Aplicar criterios de pertenencia para filtrar los hechos
        List<HechoStrategy> criterios = coleccion.getCriteriosDePertenencia();
        List<Hecho> hechosFiltrados = aplicarCriterios(hechosDeEstasFuentes, criterios);

        // 4. Establecer hechos en la colección
        coleccion.setHechos(hechosFiltrados);

        // 5. Crear entidad sin los hechos (para evitar crear nuevas entidades HechoEntity)
        utn.dds.jpa.entities.ColeccionEntity coleccionEntity = new utn.dds.jpa.entities.ColeccionEntity();
        coleccionEntity.setTitulo(coleccion.getTitulo());
        coleccionEntity.setDescripcion(coleccion.getDescripcion());
        coleccionEntity.setFuentes(fuentes);

        // 6. Buscar las HechoEntity existentes por sus UUIDs
        List<String> hechosIds = hechosFiltrados.stream()
                .map(Hecho::getUuid)
                .collect(Collectors.toList());
        List<HechoEntity> hechoEntities = buscarHechoEntitiesPorIds(hechosIds);
        coleccionEntity.setHechos(hechoEntities);

        // 7. Convertir criterios de pertenencia
        if (coleccion.getCriteriosDePertenencia() != null) {
            coleccionEntity.setCriteriosDePertenencia(
                utn.dds.mappers.CriterioMapper.toEntityList(coleccion.getCriteriosDePertenencia(), coleccionEntity.getHandle())
            );
        }

        // 8. Guardar la entidad directamente (con las relaciones ManyToMany)
        guardarColeccionConEntidades(coleccionEntity);
    }

    private List<FuenteEntity> buscarFuentesPorIds(List<String> fuentesIds) {
        if (fuentesIds == null || fuentesIds.isEmpty()) {
            return new ArrayList<>();
        }

        if (fuenteDAO instanceof Hibernate) {
            Hibernate<FuenteEntity> hibernateDAO = (Hibernate<FuenteEntity>) fuenteDAO;
            return hibernateDAO.executeQuery(em -> {
                return em.createQuery("SELECT f FROM FuenteEntity f WHERE f.uuid IN :ids", FuenteEntity.class)
                        .setParameter("ids", fuentesIds)
                        .getResultList();
            });
        }
        return new ArrayList<>();
    }

    private List<HechoEntity> buscarHechoEntitiesPorIds(List<String> hechosIds) {
        if (hechosIds == null || hechosIds.isEmpty()) {
            return new ArrayList<>();
        }

        if (hechoDAO instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) hechoDAO;
            return hibernateDAO.executeQuery(em -> {
                return em.createQuery("SELECT h FROM HechoEntity h WHERE h.uuid IN :ids", HechoEntity.class)
                        .setParameter("ids", hechosIds)
                        .getResultList();
            });
        }
        return new ArrayList<>();
    }

    private List<Hecho> buscarHechosPorOrigenFuentes(List<FuenteEntity> fuentes) {
        if (fuentes == null || fuentes.isEmpty()) {
            return new ArrayList<>();
        }

        // Extraer los UUIDs de las fuentes para buscar hechos con ese origen
        List<String> origenes = fuentes.stream()
                .map(FuenteEntity::getUuid)
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

    private void guardarColeccionConEntidades(utn.dds.jpa.entities.ColeccionEntity coleccionEntity) {
        // Crear un DAO temporal para guardar la entidad directamente
        Map<String, Object> hibernateConfig = new HashMap<>();
        hibernateConfig.put("jakarta.persistence.jdbc.url",
            System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/metamapa_db"));
        hibernateConfig.put("jakarta.persistence.jdbc.user",
            System.getenv().getOrDefault("DB_USER", "metamapa"));
        hibernateConfig.put("jakarta.persistence.jdbc.password",
            System.getenv().getOrDefault("DB_PASSWORD", "metamapa123"));
        hibernateConfig.put("persistenceUnit", "metamapa-db");

        IDAO<utn.dds.jpa.entities.ColeccionEntity> coleccionDAO =
            DAOFactory.createDAO(utn.dds.jpa.entities.ColeccionEntity.class, "hibernate", hibernateConfig);

        coleccionDAO.save(coleccionEntity);
    }

    public void actualizarColeccion(String id, Coleccion coleccionActualizada) {
        Coleccion coleccionExistente = this.coleccionRepository.obtenerPorId(id);
        if (coleccionExistente == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        this.coleccionRepository.actualizar(id, coleccionActualizada);
    }

    public void actualizarColeccionCompleta(String id, utn.dds.dto.ColeccionUpdateDTO updateDTO) {
        // Verificar que la colección existe
        Coleccion coleccionExistente = this.coleccionRepository.obtenerPorId(id);
        if (coleccionExistente == null) {
            throw new RuntimeException("Colección no encontrada");
        }

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
    }

    private void actualizarCriteriosYFuentes(String id, List<HechoStrategy> nuevosCriterios,
                                           List<String> nuevasFuentesIds, Coleccion coleccionExistente) {
        // 1. Actualizar criterios primero en la BD
        this.coleccionRepository.actualizarCriterios(id, nuevosCriterios);

        // 2. Buscar las nuevas fuentes por sus IDs
        List<FuenteEntity> nuevasFuentes = buscarFuentesPorIds(nuevasFuentesIds);

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

        List<HechoEntity> hechoEntities = hechosFiltrados.stream()
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
        List<HechoEntity> hechoEntities = hechosFiltrados.stream()
            .map(hecho -> buscarHechoEntityPorId(hecho.getUuid()))
            .filter(entity -> entity != null)
            .collect(Collectors.toList());

        this.coleccionRepository.actualizarHechos(id, hechoEntities);
    }

    private void actualizarSoloFuentes(String id, List<String> nuevasFuentesIds, Coleccion coleccionExistente) {
        // 1. Buscar las nuevas fuentes por sus IDs
        List<FuenteEntity> nuevasFuentes = buscarFuentesPorIds(nuevasFuentesIds);

        // 2. Buscar hechos de las nuevas fuentes
        List<Hecho> hechosNuevasFuentes = buscarHechosPorOrigenFuentes(nuevasFuentes);

        // 3. Obtener hechos existentes de la colección actual
        List<Hecho> hechosExistentes = coleccionExistente.getHechos();

        // 4. Combinar hechos existentes + nuevos
        List<Hecho> todosLosHechos = new ArrayList<>(hechosExistentes);
        todosLosHechos.addAll(hechosNuevasFuentes);

        // 5. Aplicar criterios existentes si los hay
        List<HechoStrategy> criteriosExistentes = coleccionExistente.getCriteriosDePertenencia();
        List<Hecho> hechosFiltrados = aplicarCriterios(todosLosHechos, criteriosExistentes);

        // 6. Actualizar fuentes y hechos en la BD
        this.coleccionRepository.actualizarFuentes(id, nuevasFuentes);

        List<HechoEntity> hechoEntities = hechosFiltrados.stream()
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

    private HechoEntity buscarHechoEntityPorId(String uuid) {
        if (hechoDAO instanceof Hibernate) {
            Hibernate<HechoEntity> hibernateDAO = (Hibernate<HechoEntity>) hechoDAO;
            return hibernateDAO.findById(uuid);
        }
        return null;
    }

    public void eliminarColeccion(String id) {
        Coleccion coleccionExistente = this.coleccionRepository.obtenerPorId(id);
        if (coleccionExistente == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        this.coleccionRepository.eliminar(id);
    }

    public List<Hecho> buscarHechosEnColeccion(String id, List<HechoStrategy> filtros) {
        Coleccion coleccion = this.coleccionRepository.obtenerPorId(id);
        if (coleccion == null) {
            throw new RuntimeException("Colección no encontrada");
        }
        return coleccion.buscarHechos(filtros);
    }

    public RespuestaPaginadaDTO<HechoDTO> obtenerHechosDeColeccionPaginado(String handle, int page, int size) {
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        // Verificar que la colección existe
        Coleccion coleccion = this.coleccionRepository.obtenerPorId(handle);
        if (coleccion == null) {
            throw new RuntimeException("Colección no encontrada");
        }

        // Usar HechoRepository para consulta directa optimizada
        return this.hechoRepository.obtenerHechosDeColeccionPaginado(handle, page, size);
    }

    public RespuestaPaginadaDTO<HechoDTO> obtenerHechosDeColeccionPaginado(String handle, int page, int size,
                                                                         String categoria, String estado, String etiquetas) {
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        // Verificar que la colección existe
        Coleccion coleccion = this.coleccionRepository.obtenerPorId(handle);
        if (coleccion == null) {
            throw new RuntimeException("Colección no encontrada");
        }

        // Crear filtros a partir de query params
        List<HechoStrategy> filtros = crearFiltrosDesdeQueryParams(categoria, estado, etiquetas);

        if (filtros.isEmpty()) {
            // Sin filtros, usar método optimizado
            return this.hechoRepository.obtenerHechosDeColeccionPaginado(handle, page, size);
        } else {
            // Con filtros, usar método que aplica filtros
            return this.hechoRepository.buscarHechosEnColeccionPaginado(handle, filtros, page, size);
        }
    }

    private List<HechoStrategy> crearFiltrosDesdeQueryParams(String categoria, String estado, String etiquetas) {
        List<HechoStrategy> filtros = new ArrayList<>();

        if (categoria != null && !categoria.trim().isEmpty()) {
            filtros.add(new CategoriaStrategy(categoria.trim()));
        }

        if (estado != null && !estado.trim().isEmpty()) {
            try {
                EstadoHecho estadoHecho = EstadoHecho.valueOf(estado.trim().toUpperCase());
                filtros.add(new EstadoStrategy(estadoHecho));
            } catch (IllegalArgumentException e) {
                // Ignorar estados inválidos
            }
        }

        if (etiquetas != null && !etiquetas.trim().isEmpty()) {
            List<String> listaEtiquetas = Arrays.stream(etiquetas.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            if (!listaEtiquetas.isEmpty()) {
                filtros.add(new EtiquetasStrategy(listaEtiquetas));
            }
        }

        return filtros;
    }

    public RespuestaPaginadaDTO<HechoDTO> buscarHechosEnColeccionPaginado(String handle, List<HechoStrategy> filtros, int page, int size) {
        // Validar parámetros
        if (page < 0) page = 0;
        if (size <= 0 || size > 100) size = 10;

        // Verificar que la colección existe
        Coleccion coleccion = this.coleccionRepository.obtenerPorId(handle);
        if (coleccion == null) {
            throw new RuntimeException("Colección no encontrada");
        }

        // Usar HechoRepository para consulta directa optimizada
        return this.hechoRepository.buscarHechosEnColeccionPaginado(handle, filtros, page, size);
    }
}
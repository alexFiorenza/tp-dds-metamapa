package utn.dds.agregador.graphql;

import graphql.schema.DataFetcher;
import utn.dds.agregador.controller.FiltroFactory;
import utn.dds.agregador.service.ServiceAgregador;
import utn.dds.agregador.service.ServiceRegistry;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.ResultadoAgregacionDTO;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.FuenteRequestDTO;
import utn.dds.dto.HechoDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Clase que contiene todos los DataFetchers para GraphQL
 * Los DataFetchers son funciones que obtienen datos para cada campo del schema
 */
public class GraphQLDataFetchers {

    private final ServiceAgregador serviceAgregador;
    private final ServiceRegistry serviceRegistry;

    public GraphQLDataFetchers(ServiceAgregador serviceAgregador, ServiceRegistry serviceRegistry) {
        this.serviceAgregador = serviceAgregador;
        this.serviceRegistry = serviceRegistry;
    }

    // ==================== QUERIES ====================

    /**
     * Query: hechos
     * Obtiene hechos paginados con filtros opcionales
     */
    public DataFetcher<Map<String, Object>> getHechosPaginados() {
        return environment -> {
            Integer pagina = environment.getArgument("pagina");
            Integer tamanioPagina = environment.getArgument("tamanioPagina");
            Map<String, Object> filtrosMap = environment.getArgument("filtros");

            if (pagina == null) pagina = 0;
            if (tamanioPagina == null) tamanioPagina = 10;

            // Crear filtros desde el Map de GraphQL
            List<HechoStrategy> filtros = new ArrayList<>();
            if (filtrosMap != null && !filtrosMap.isEmpty()) {
                filtros = FiltroFactory.crearFiltrosDesdeMap(filtrosMap);
            }

            RespuestaPaginadaDTO<HechoDTO> respuesta = serviceAgregador.obtenerHechos(filtros, pagina, tamanioPagina);

            // Convertir a Map para GraphQL
            Map<String, Object> resultado = new HashMap<>();
            resultado.put("datos", respuesta.getDatos().stream()
                .map(this::hechoDTOToMap)
                .collect(Collectors.toList()));
            resultado.put("pagina", respuesta.getPagina());
            resultado.put("tamanioPagina", respuesta.getTamanioPagina());
            resultado.put("totalElementos", respuesta.getTotalElementos());
            resultado.put("totalPaginas", respuesta.getTotalPaginas());
            resultado.put("tieneSiguiente", respuesta.isTieneSiguiente());
            resultado.put("tieneAnterior", respuesta.isTieneAnterior());

            return resultado;
        };
    }

    /**
     * Query: fuentes
     * Obtiene todas las fuentes registradas
     */
    public DataFetcher<List<FuenteDTO>> getFuentes() {
        return environment -> serviceRegistry.obtenerTodasLasFuentes();
    }

    /**
     * Query: fuentesPorHost
     * Obtiene fuentes por host
     */
    public DataFetcher<List<FuenteDTO>> getFuentesPorHost() {
        return environment -> {
            String host = environment.getArgument("host");
            return serviceRegistry.obtenerFuentesPorHost(host);
        };
    }

    // ==================== MUTATIONS ====================

    /**
     * Mutation: ejecutarAgregacion
     * Ejecuta el proceso de agregación
     */
    public DataFetcher<ResultadoAgregacionDTO> ejecutarAgregacion() {
        return environment -> serviceAgregador.agregacion();
    }

    /**
     * Mutation: registrarFuente
     * Registra una nueva fuente
     */
    public DataFetcher<FuenteDTO> registrarFuente() {
        return environment -> {
            Map<String, Object> input = environment.getArgument("input");

            // Crear FuenteDTO para registrar
            FuenteDTO fuente = new FuenteDTO(
                (String) input.get("host"),
                (Map<String, Object>) input.get("params"),
                null, // UUID se genera automáticamente
                (String) input.get("tipo"),
                (Map<String, Object>) input.get("metadata")
            );

            serviceRegistry.registrar(fuente);

            // Buscar la fuente recién registrada para devolver con UUID
            List<FuenteDTO> fuentes = serviceRegistry.obtenerFuentesPorHost(fuente.getHost());
            if (!fuentes.isEmpty()) {
                return fuentes.get(fuentes.size() - 1); // Devolver la última registrada
            }
            return fuente;
        };
    }

    /**
     * Mutation: actualizarFuente
     * Actualiza una fuente existente
     */
    public DataFetcher<FuenteDTO> actualizarFuente() {
        return environment -> {
            String uuidStr = environment.getArgument("uuid");
            Map<String, Object> input = environment.getArgument("input");

            java.util.UUID uuid = java.util.UUID.fromString(uuidStr);

            // Crear FuenteDTO con los datos actualizados
            FuenteDTO fuenteActualizada = new FuenteDTO(
                (String) input.get("host"),
                (Map<String, Object>) input.get("params"),
                uuid,
                (String) input.get("tipo"),
                (Map<String, Object>) input.get("metadata")
            );

            serviceRegistry.actualizar(fuenteActualizada);

            // Devolver la fuente actualizada
            return serviceRegistry.obtenerFuentePorUuid(uuid);
        };
    }

    /**
     * Mutation: eliminarFuente
     * Elimina una fuente (oculta sus hechos)
     */
    public DataFetcher<Boolean> eliminarFuente() {
        return environment -> {
            String uuidStr = environment.getArgument("uuid");
            java.util.UUID uuid = java.util.UUID.fromString(uuidStr);
            return serviceRegistry.eliminarFuentePorUuid(uuid);
        };
    }

    // ==================== HELPERS ====================

    /**
     * Convierte un Hecho a Map para GraphQL
     */
    private Map<String, Object> hechoToMap(Hecho hecho) {
        Map<String, Object> map = new HashMap<>();
        map.put("uuid", hecho.getUuid());
        map.put("titulo", hecho.getTitulo());
        map.put("descripcion", hecho.getDescripcion());
        map.put("categoria", hecho.getCategoria());
        map.put("fechaAcontecimiento", hecho.getFechaAcontecimiento());
        map.put("origen", hecho.getOrigen());
        map.put("tipo", hecho.getTipo());
        map.put("fechaCarga", hecho.getFechaCarga());
        map.put("estado", hecho.getEstado());

        // Ubicación
        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitud", hecho.getLatitud());
        ubicacion.put("longitud", hecho.getLongitud());
        map.put("ubicacion", ubicacion);

        // Contribuyente
        if (hecho.getContribuyente() != null) {
            Map<String, Object> contribuyente = new HashMap<>();
            contribuyente.put("userId", hecho.getContribuyente().getUserId());
            contribuyente.put("nombre", hecho.getContribuyente().getNombre());
            map.put("contribuyente", contribuyente);
        }

        return map;
    }

    /**
     * Convierte un HechoDTO a Map para GraphQL
     */
    private Map<String, Object> hechoDTOToMap(HechoDTO hecho) {
        Map<String, Object> map = new HashMap<>();
        map.put("uuid", hecho.getUuid());
        map.put("titulo", hecho.getTitulo());
        map.put("descripcion", hecho.getDescripcion());
        map.put("categoria", hecho.getCategoria());
        map.put("fechaAcontecimiento", hecho.getFechaAcontecimiento());
        map.put("origen", hecho.getOrigen());
        map.put("tipo", hecho.getTipo());
        map.put("fechaCarga", hecho.getFechaCarga());
        map.put("estado", hecho.getEstado());

        // Ubicación
        Map<String, Object> ubicacion = new HashMap<>();
        ubicacion.put("latitud", hecho.getLatitud());
        ubicacion.put("longitud", hecho.getLongitud());
        map.put("ubicacion", ubicacion);

        // Contribuyente
        if (hecho.getContribuyente() != null) {
            Map<String, Object> contribuyente = new HashMap<>();
            contribuyente.put("userId", hecho.getContribuyente().getUserId());
            contribuyente.put("nombre", hecho.getContribuyente().getNombre());
            map.put("contribuyente", contribuyente);
        }

        return map;
    }
}

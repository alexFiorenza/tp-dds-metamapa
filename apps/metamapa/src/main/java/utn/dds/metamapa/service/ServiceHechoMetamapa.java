package utn.dds.metamapa.service;

import utn.dds.metamapa.persistencia.HechoRepository;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.RespuestaPaginadaDTO;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServiceHechoMetamapa {
    private final HechoRepository hechoRepository;

    public ServiceHechoMetamapa(String daoType, Map<String, Object> daoConfig) {
        this.hechoRepository = new HechoRepository(daoConfig);
    }

    public List<Hecho> obtenerHechos(List<HechoStrategy> filtros) {
        List<Hecho> todosLosHechos = this.hechoRepository.obtenerTodos();

        if (filtros == null || filtros.isEmpty()) {
            return todosLosHechos;
        }

        return todosLosHechos.stream()
                .filter(hecho -> filtros.stream().allMatch(filtro -> filtro.cumple(hecho)))
                .collect(Collectors.toList());
    }

    public RespuestaPaginadaDTO<HechoDTO> obtenerHechosPaginados(List<HechoStrategy> filtros, int pagina, int tamanioPagina) {
        // Obtener todos los hechos de la base de datos
        List<Hecho> todosLosHechos = this.hechoRepository.obtenerTodos();

        // Aplicar filtros si existen
        List<Hecho> hechosFiltrados = todosLosHechos;
        if (filtros != null && !filtros.isEmpty()) {
            hechosFiltrados = todosLosHechos.stream()
                    .filter(hecho -> filtros.stream().allMatch(filtro -> filtro.cumple(hecho)))
                    .collect(Collectors.toList());
        }

        // Calcular paginación
        long totalElementos = hechosFiltrados.size();
        int inicio = pagina * tamanioPagina;
        int fin = Math.min(inicio + tamanioPagina, hechosFiltrados.size());

        List<Hecho> hechosPaginados = hechosFiltrados.subList(inicio, fin);

        // Convertir a DTO
        List<HechoDTO> hechosDTO = hechosPaginados.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());

        return new RespuestaPaginadaDTO<>(hechosDTO, pagina, tamanioPagina, totalElementos);
    }

    public void reportarHecho(String uuidHecho) {
        Hecho hecho = this.hechoRepository.obtenerPorId(uuidHecho);
        if (hecho == null) {
            throw new RuntimeException("Hecho no encontrado");
        }

        // Lógica para reportar un hecho (marcarlo como oculto)
        this.hechoRepository.cambiarEstado(uuidHecho, utn.dds.dominio.EstadoHecho.OCULTO);
    }
}
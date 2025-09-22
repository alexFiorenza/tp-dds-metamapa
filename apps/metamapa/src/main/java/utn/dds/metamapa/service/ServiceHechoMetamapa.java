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

    public RespuestaPaginadaDTO<HechoDTO> obtenerHechos(List<HechoStrategy> filtros, int pagina, int tamanioPagina) {
        RespuestaPaginadaDTO<Hecho> respuestaHechos = this.hechoRepository.obtenerConFiltros(filtros, pagina, tamanioPagina);

        // Convertir a DTO
        List<HechoDTO> hechosDTO = respuestaHechos.getElementos().stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());

        return new RespuestaPaginadaDTO<>(hechosDTO, pagina, tamanioPagina, respuestaHechos.getTotalElementos());
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
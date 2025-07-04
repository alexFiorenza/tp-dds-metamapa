package utn.dds.fuentes.proxy.metamapa;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;

import java.util.List;
import java.util.stream.Collectors;

public class ControllerProxyMetamapa {
    private final ServiceFuenteProxyMetaMapa proxyMetamapaService;

    public ControllerProxyMetamapa(ServiceFuenteProxyMetaMapa proxyMetamapaService) {
        this.proxyMetamapaService = proxyMetamapaService;
    }

    public void obtenerHechos(Context ctx) {
        try {
            List<Hecho> hechos = proxyMetamapaService.obtenerHechos();
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            ctx.json(hechosDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }
}
package utn.dds.fuentes.proxy.metamapa.controller;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.proxy.metamapa.service.ServiceFuenteProxyMetaMapa;

import java.util.List;
import java.util.stream.Collectors;

public class ControllerProxyMetamapa {
    private final ServiceFuenteProxyMetaMapa proxyMetamapaService;

    public ControllerProxyMetamapa(String url) {
        this.proxyMetamapaService = new ServiceFuenteProxyMetaMapa(url);
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
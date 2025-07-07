package utn.dds.fuentes.proxy.demo.controller;

import io.javalin.http.Context;
import utn.dds.fuentes.proxy.demo.service.ServiceFuenteProxyDemo;
import utn.dds.fuentes.proxy.demo.service.model.conexion.ConexionExampleIml;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.daos.IDAO;

import java.net.MalformedURLException;
import java.util.List;
import java.util.stream.Collectors;

public class ControllerFuenteProxyDemo {
    private final ServiceFuenteProxyDemo serviceFuenteProxyDemo;

    public ControllerFuenteProxyDemo(IDAO<String> dao) throws MalformedURLException {
       this.serviceFuenteProxyDemo = new ServiceFuenteProxyDemo(new ConexionExampleIml(), dao);
    }

    public void obtenerHechos(Context ctx) {
        List<Hecho> hechos = this.serviceFuenteProxyDemo.obtenerHechos();
        List<HechoDTO> hechosDTO = hechos.stream()
            .map(HechoDTO::fromHecho)
            .collect(Collectors.toList());
        ctx.json(hechosDTO);
    }

    public void actualizarCache(Context ctx) {
        this.serviceFuenteProxyDemo.actualizarCache();
        ctx.status(200);
    }
}

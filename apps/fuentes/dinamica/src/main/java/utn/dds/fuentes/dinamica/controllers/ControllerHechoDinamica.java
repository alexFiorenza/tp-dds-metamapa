package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.conexion.Conexion;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerHechoDinamica {
    private final ServiceHechoDinamica dinamicaService;


    // Hay que ver si esta bien implementada
    public ControllerHechoDinamica(String daoType, Map<String, Object> daoConfig, Conexion conexion) throws MalformedURLException {
        this.dinamicaService = new ServiceHechoDinamica(daoType, daoConfig, conexion);
    }

    public void obtenerHechos(Context ctx) {
        try {
            List<Hecho> hechos = dinamicaService.obtenerHechos();
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            ctx.json(hechosDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }

    public void agregarHecho(Context ctx, HechoDTO hechoDTO) {
        try {
            // convertir el hechoDTO a hecho normal
            Hecho hecho = hechoDTO.toHecho();
            dinamicaService.aportarHecho(hecho);
            ctx.json(hechoDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar el hecho: " + e.getMessage());
        }
    }
}
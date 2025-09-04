package utn.dds.fuentes.dinamica.controllers;

import io.javalin.http.Context;
import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.fuentes.dinamica.FuenteDinamicaImpl;
import utn.dds.fuentes.dinamica.Main;
import utn.dds.fuentes.dinamica.conexion.Conexion;
import utn.dds.fuentes.dinamica.services.ServiceHechoDinamica;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ControllerHechoDinamica {
    private final ServiceHechoDinamica dinamicaService;
    private static final Logger loggerControllerDinamica = LoggerFactory.getLogger(ControllerHechoDinamica.class);

    // Hay que ver si esta bien implementada
    public ControllerHechoDinamica(String daoType, Map<String, Object> daoConfig) throws MalformedURLException {
        this.dinamicaService = new ServiceHechoDinamica(daoType, daoConfig);
    }

    public void obtenerHechos(Context ctx) {
        try {
            loggerControllerDinamica.info("Obteniendo hechos....");
            List<Hecho> hechos = this.dinamicaService.obtenerHechos();
            List<HechoDTO> hechosDTO = hechos.stream()
                .map(HechoDTO::fromHecho)
                .collect(Collectors.toList());
            ctx.json(hechosDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al obtener hechos: " + e.getMessage());
        }
    }

    public void agregarHecho(Context ctx) {
        try {
            // convertir el hechoDTO a hecho normal
            loggerControllerDinamica.info("Agregando el hecho....");
            HechoDTO hechoDTO = ctx.bodyValidator(HechoDTO.class)
                    .get();
            Hecho hecho = hechoDTO.toHecho();   // Esto es por si lo usamos despues
            dinamicaService.aportarHecho(hechoDTO);
            ctx.status(201);
            ctx.json(hechoDTO);
        } catch (Exception e) {
            ctx.status(500).result("Error al agregar el hecho: " + e.getMessage());
        }
    }
}
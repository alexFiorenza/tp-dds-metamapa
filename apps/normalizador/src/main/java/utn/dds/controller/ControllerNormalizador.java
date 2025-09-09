package utn.dds.controller;

import io.javalin.http.Context;
import utn.dds.dto.HechoDTO;
import utn.dds.service.ServiceNormalizador;

import java.util.Map;

public class ControllerNormalizador {
    private static final ServiceNormalizador serviceNormalizador = new ServiceNormalizador();

    public void normalizar(Context ctx) {
        try {
            HechoDTO hechoDTO = ctx.bodyValidator(HechoDTO.class).get();

            HechoDTO hechoNormalizadoDTO = ServiceNormalizador.normalizar(hechoDTO);

            ctx.status(201).json(hechoNormalizadoDTO);
        } catch (io.javalin.validation.ValidationException ve) {
        ve.printStackTrace();
        ctx.status(400).json(Map.of(
                "error", "Error de validación en el body",
                "exception", ve.getClass().getName(),
                "details", ve.getErrors() // 👈 esto muestra qué campo falló
        ));
    } catch (Exception e) {
        e.printStackTrace();
        ctx.status(500).json(Map.of(
                "error", "Error inesperado",
                "exception", e.getClass().getName(),
                "message", e.getMessage()
        ));}
    }
}


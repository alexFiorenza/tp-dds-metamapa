package utn.dds.controller;


import io.javalin.http.Context;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.normalizaciones.NormalizadorCategoria;
import utn.dds.service.ServiceNormalizador;

public class ControllerNormalizador {
    private static final ServiceNormalizador serviceNormalizador = new ServiceNormalizador();
    public void Normalizar(Context ctx) {
        try {
            HechoDTO hechoDTO = ctx.bodyValidator(HechoDTO.class)
                    .get();

            HechoDTO hechoNormalizadoDTO = ServiceNormalizador.normalizar(hechoDTO);
            ctx.status(201);
            ctx.json(hechoNormalizadoDTO);
        }
        catch (Exception e) {
            ctx.status(500).result("Error al normalizar hecho: " + e.getMessage());
        }
    }
}

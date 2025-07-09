package utn.dds.metamapa;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;

public class ControllerHechoPublico {
    public static void obtenerHechos(Context ctx) {
        String categoria = ctx.queryParam("categoria");
        String fechaReporteDesde = ctx.queryParam("fecha_reporte_desde");
        String fechaReporteHasta = ctx.queryParam("fecha_reporte_hasta");
        String fechaAcontecimientoDesde = ctx.queryParam("fecha_acontecimiento_desde");
        String fechaAcontecimientoHasta = ctx.queryParam("fecha_acontecimiento_hasta");
    }
}

package utn.dds.metamapa;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;

import java.util.List;

public class ControllerColeccionPublico {
    private final ServiceColeccion serviceColeccion;

    public ControllerColeccionPublico(){
        this.serviceColeccion=new ServiceColeccion();
    }

    public List<Hecho>  obtenerColeccion(Context ctx) {
        String coleccionId = ctx.pathParam("identificador");

        String categoria = ctx.queryParam("categoria");
        String fechaReporteDesde = ctx.queryParam("fecha_reporte_desde");
        String fechaReporteHasta = ctx.queryParam("fecha_reporte_hasta");
        String fechaAcontecimientoDesde = ctx.queryParam("fecha_acontecimiento_desde");
        String fechaAcontecimientoHasta = ctx.queryParam("fecha_acontecimiento_hasta");

        List<Hecho> hechosColeccion=this.serviceColeccion.buscarColeccion(coleccionId,
                categoria,fechaReporteDesde,fechaReporteHasta,fechaAcontecimientoDesde,fechaAcontecimientoHasta);
        return map.hechosColeccion.toDTO();
    }
}

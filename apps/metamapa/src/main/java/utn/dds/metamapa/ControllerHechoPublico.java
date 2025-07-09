package utn.dds.metamapa;

import io.javalin.http.Context;
import utn.dds.dominio.Hecho;

import java.util.List;

public class ControllerHechoPublico {
    private final ServiceHecho serviceHecho;
    public ControllerHechoPublico(){
        this.serviceHecho=new ServiceHecho();
    }
    public List<Hecho> obtenerHechos(Context ctx) {
        String categoria = ctx.queryParam("categoria");
        String fechaReporteDesde = ctx.queryParam("fecha_reporte_desde");
        String fechaReporteHasta = ctx.queryParam("fecha_reporte_hasta");
        String fechaAcontecimientoDesde = ctx.queryParam("fecha_acontecimiento_desde");
        String fechaAcontecimientoHasta = ctx.queryParam("fecha_acontecimiento_hasta");
        //TODO cambiar los string para que sean strategy ? sino pasarlos como string y que el service se encargue de fijarse eso?
        return this.serviceHecho.buscarHechos();
    }
}

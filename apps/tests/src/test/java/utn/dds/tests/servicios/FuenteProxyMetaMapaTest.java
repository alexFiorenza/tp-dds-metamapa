package utn.dds.tests.servicios;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.TipoHecho;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class FuenteProxyMetaMapaTest {

    // datos de ejemplo en memoria
    private List<Hecho> seed() {
        List<Hecho> s = new ArrayList<>();
        s.add(new Hecho("A","d","incendio",LocalDate.now(),"origen1",null,TipoHecho.TEXTO,0,0,LocalDateTime.now(),EstadoHecho.ACTIVO,new ArrayList<>(),new ArrayList<>()));
        s.add(new Hecho("B","d","robo",LocalDate.now(),"origen2",null,TipoHecho.TEXTO,0,0,LocalDateTime.now(),EstadoHecho.ACTIVO,new ArrayList<>(),new ArrayList<>()));
        return s;
    }

    private List<Hecho> filtrarPorParams(List<Hecho> lista, Map<String,String> params) {
        if (params == null || params.isEmpty()) return lista;
        List<Hecho> out = new ArrayList<>();
        for (Hecho h : lista) {
            boolean ok = true;
            if (params.containsKey("categoria")) {
                ok &= params.get("categoria").equalsIgnoreCase(h.getCategoria());
            }
            if (params.containsKey("origen")) {
                ok &= params.get("origen").equalsIgnoreCase(h.getOrigen());
            }
            if (ok) out.add(h);
        }
        return out;
    }

    @Test
    void obtieneHechosViaRest() {
        List<Hecho> datos = seed();
        assertFalse(datos.isEmpty());
        assertEquals(2, datos.size());
    }

    @Test
    void filtraHechosPorParametros() {
        List<Hecho> datos = seed();
        Map<String,String> p = Map.of("categoria","incendio");
        List<Hecho> filtrados = filtrarPorParams(datos, p);
        assertEquals(1, filtrados.size());
        assertEquals("incendio", filtrados.get(0).getCategoria());
    }
}
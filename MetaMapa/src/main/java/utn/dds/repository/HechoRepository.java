package utn.dds.repository;
import utn.dds.model.*;
import utn.dds.model.fuentes.FuenteDeDatos;
import utn.dds.model.fuentes.estatica.strategies.ProcesadorStrategy;
import utn.dds.model.fuentes.proxy.Conexion;
import utn.dds.model.fuentes.proxy.FuenteDemo;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HechoRepository {
    public List<Hecho> desdeEstatica(ProcesadorStrategy procesador){
        // Suponemos que todas las estaticas son archivos con distintas estrategias de procesamiento
        return procesador.obtenerHechos();
    }

    // programar esta
    public List<Hecho> desdeDinamica(String _uuid){
        // Aca por ejemplo seria obtener desde la base de datos
        // hechos = orm.find("hechos")
        // hechos -> transform -> List<Hecho>
        throw new UnsupportedOperationException("Metodo no implementado");
    }

    public List<Hecho> desdeProxy(FuenteDeDatos _fuente){
        return _fuente.obtenerHechos();
    }

    public Hecho persistirHecho(Contribuyente contribuyente, String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                                    Origen origen, TipoHecho tipo,
                                    double longitud, double latitud, LocalDateTime fechaCarga,
                                    EstadoHecho estado, List<String> etiquetas){
        // Más adelante tendremos que conectarnos a la db y guardar acá
        return new Hecho(titulo, descripcion, categoria, fechaAcontecimiento,
                origen, contribuyente, tipo, longitud, latitud, fechaCarga,
                estado, etiquetas);
    }
}

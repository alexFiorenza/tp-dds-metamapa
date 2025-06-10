package utn.dds.repository;
import utn.dds.model.*;
import utn.dds.model.fuentes.FuenteDeDatos;
import utn.dds.model.fuentes.estatica.strategies.ProcesadorStrategy;

import java.time.LocalDateTime;
import java.util.List;

public class HechoRepository {
    public List<Hecho> desdeEstatica(ProcesadorStrategy procesador){
        return procesador.obtenerHechos();
    }

    public List<Hecho> desdeDinamica(String _uuid){
        // Aca por ejemplo seria obtener desde la base de datos
        // hechos = orm.find("hechos")
        // hechos -> transform -> List<Hecho>
        throw new UnsupportedOperationException("Metodo no implementado");
    }

    public Hecho nuevoHechoDinamico(Contribuyente contribuyente, String titulo, String descripcion, String categoria, LocalDateTime fechaAcontecimiento,
                                    Origen origen, TipoHecho tipo,
                                    double longitud, double latitud, LocalDateTime fechaCarga,
                                    EstadoHecho estado, List<String> etiquetas){

        Hecho nuevoHecho = new Hecho(titulo, descripcion, categoria, fechaAcontecimiento,
                origen, contribuyente, tipo, longitud, latitud, fechaCarga,
                estado, etiquetas);

        return nuevoHecho;
    }
}

package utn.dds.service.negocio;

import utn.dds.model.*;
import utn.dds.repository.HechoRepository;

import java.time.LocalDate;
import java.util.List;

public class ContribuyenteService {

    // Una opcion que no se si se puede hacer es, pasarle un contribuyente y un paquete de datos (titulo, descripcion, etc)
    public Hecho aportarHecho(Contribuyente contribuyente, String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                              Origen origen, TipoHecho tipo,
                              double longitud, double latitud, LocalDate fechaCarga,
                              EstadoHecho estado, List<String> etiquetas){
        HechoRepository hechoRepository = new HechoRepository();

        Hecho hecho = hechoRepository.nuevoHechoDinamico(contribuyente, titulo, descripcion, categoria, fechaAcontecimiento,
                origen, tipo, longitud, latitud, fechaCarga, estado, etiquetas);

        return hecho;
    }
    /*
    public List<Hecho>obtenerHechos(int Hora){
        // usar un filter para que me devuelva todos los hechos mayores o menores a esa hora
    }
     */
}

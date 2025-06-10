package utn.dds.service.negocio;

import utn.dds.model.*;
import utn.dds.repository.HechoRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ContribuyenteService {

    // Una opcion que no se si se puede hacer es, pasarle un contribuyente y un paquete de datos (titulo, descripcion, etc)
    public Hecho aportarHecho(Contribuyente contribuyente, String titulo, String descripcion, String categoria, LocalDateTime fechaAcontecimiento,
                              Origen origen, TipoHecho tipo,
                              double longitud, double latitud, LocalDateTime fechaCarga,
                              EstadoHecho estado, List<String> etiquetas){
        HechoRepository hechoRepository = new HechoRepository();

        Hecho hecho = hechoRepository.nuevoHechoDinamico(contribuyente, titulo, descripcion, categoria, fechaAcontecimiento,
                origen, tipo, longitud, latitud, fechaCarga, estado, etiquetas);

        return hecho;
    }

    // Hay que toquetearla, fijarse la coleccion
    public List<Hecho>obtenerHechosRecientes(Coleccion coleccion){  // Hay que instanciar la coleccion de donde queremos obtener los primeros hechos
        List<Hecho> hechosRecientes = coleccion.getHechos().stream()
                .filter(hecho -> hecho.getOrigen() == Origen.CONTRIBUYENTE) // esto puede no estar porque ya nos pasan la fuente
                .filter(hecho -> Duration.between(hecho.getFechaCarga(), LocalDateTime.now()).toHours() < 1)
                .collect(Collectors.toList());

        return hechosRecientes;
    }

}

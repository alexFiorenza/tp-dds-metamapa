package utn.dds.service.negocio;

import utn.dds.model.*;

import java.time.LocalDate;
import java.util.List;

public class ContribuyenteService {
    // No se si se deberian pasar las fuentes por atributo, y tampoco si esto seria repeticion de codigo por lo de Hecho
    public Hecho aportarHecho(Contribuyente contribuyente, String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                           Origen origen, TipoHecho tipo,
                           double longitud, double latitud, LocalDate fechaCarga,
                           EstadoHecho estado, List<String> etiquetas){

        Hecho nuevoHecho = new Hecho(titulo, descripcion, categoria, fechaAcontecimiento,
                origen, contribuyente, tipo, longitud, latitud, fechaCarga,
                estado, etiquetas);

        return nuevoHecho;

        // aca no se si añadirlo directamente a la lista de aportes, o si tengo que hacer otrafuncion para
        // saber si fue aceptada la subida del hecho o no
        //aportes.add(nuevoHecho);
    }

    /*
    public List<Hecho>obtenerHechos(int Hora){
        // usar un filter para que me devuelva todos los hechos mayores o menores a esa hora
    }
     */
}

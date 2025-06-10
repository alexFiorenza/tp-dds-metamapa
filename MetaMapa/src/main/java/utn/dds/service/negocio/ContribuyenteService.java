package utn.dds.service.negocio;

import utn.dds.model.*;
import utn.dds.repository.HechoRepository;

import java.time.LocalDate;
import java.util.List;

public class ContribuyenteService {

    public Hecho aportarHecho(){
        HechoRepository hechoRepository = new HechoRepository();
        Hecho hecho = hechoRepository.nuevoHechoDinamico();  // hay que programar esta (no se como pasarlelos datos del contribuyente)
        return hecho;
    }
    /*
    public List<Hecho>obtenerHechos(int Hora){
        // usar un filter para que me devuelva todos los hechos mayores o menores a esa hora
    }
     */
}

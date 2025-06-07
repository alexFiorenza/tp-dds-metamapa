package utn.dds.service.negocio;
import utn.dds.model.Hecho;
import utn.dds.model.fuentes.estatica.strategies.ProcesadorStrategy;
import utn.dds.repository.HechoRepository;
import java.util.List;

public class FuenteDeDatosService {
    private final HechoRepository hechoRepository;

    public FuenteDeDatosService() {
        this.hechoRepository = new HechoRepository();
    }

    public List<Hecho>  importarDesdeArchivo(ProcesadorStrategy procesador){
       return this.hechoRepository.desdeEstatica(procesador);
    }
}

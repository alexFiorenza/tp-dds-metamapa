package utn.dds.service.negocio;
    import utn.dds.model.Hecho;
import utn.dds.model.fuentes.estatica.FuenteEstatica;
import utn.dds.model.fuentes.estatica.strategies.ProcesadorStrategy;
import java.util.List;

public class FuenteDeDatos {
    public List<Hecho> importarDesdeEstatica(ProcesadorStrategy procesador){
        FuenteEstatica fuente = new FuenteEstatica(procesador);
        return fuente.obtenerHechos();
    }
}

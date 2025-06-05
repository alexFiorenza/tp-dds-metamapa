package utn.dds.service.negocio;

import utn.dds.model.Coleccion;
import utn.dds.model.Hecho;
import utn.dds.model.HechoStrategy;
import utn.dds.model.fuentes.estatica.FuenteEstatica;

import java.util.List;

public class ColeccionService {
    private final Coleccion coleccion;

    public ColeccionService(String titulo, String descripcion,
                            List<Hecho> hechos, List<HechoStrategy> criteriosDePertenencia) {
        this.coleccion = new Coleccion(titulo, descripcion, hechos, criteriosDePertenencia);
    }

    public void importarDesdeFuenteEstatica(FuenteEstatica fuenteEstatica){
        List<Hecho> hechosImportados =  this.aplicarCriterios(fuenteEstatica.obtenerHechos());
        coleccion.setHechos(hechosImportados);
    }

    private List<Hecho> aplicarCriterios(List<Hecho> hechos){
        // TODO: Hay que hacer las estrategias y aplicarlas aca
        List<HechoStrategy> criteriosDePertenencia = this.coleccion.getCriteriosDePertenencia();

        return hechos;
    }
}

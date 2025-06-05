package utn.dds.service.negocio;

import utn.dds.model.Coleccion;
import utn.dds.model.Hecho;
import utn.dds.model.HechoStrategy;
import java.util.List;
import java.util.stream.Collectors;

public class ColeccionService {
    private final Coleccion coleccion;

    public ColeccionService(String titulo, String descripcion,
                            List<Hecho> hechos, List<HechoStrategy> criteriosDePertenencia) {
        this.coleccion = new Coleccion(titulo, descripcion, hechos, criteriosDePertenencia);
    }

    public void cargarHechos(List<Hecho> hechos) {
        coleccion.setHechos(this.aplicarCriterios(hechos));
    }

    private List<Hecho> aplicarCriterios(List<Hecho> hechos){
        List<HechoStrategy> criteriosDePertenencia = this.coleccion.getCriteriosDePertenencia();

        return hechos.stream()
                .filter(hecho -> criteriosDePertenencia.stream().allMatch(criterio -> criterio.cumple(hecho)))
                .collect(Collectors.toList());
    }

    public List<Hecho> hechos() {
        return List.copyOf(coleccion.getHechos()); // Devuelvo una copia, para que no me modifique la referencia
    }

    public List<Hecho> buscarHechos(List<HechoStrategy> filtros){
        return List.copyOf(coleccion.buscarHechos(filtros));
    }
}

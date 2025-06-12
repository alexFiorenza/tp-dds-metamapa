package utn.dds.service.negocio;

import utn.dds.model.Coleccion;
import utn.dds.model.Hecho;
import utn.dds.model.criterios.HechoStrategy;
import utn.dds.repository.ColeccionRepository;

import java.util.List;
import java.util.stream.Collectors;

public class ColeccionService {
    private final ColeccionRepository coleccionRepository;

    public ColeccionService() {
        this.coleccionRepository = new ColeccionRepository();
    }

    public Coleccion nuevaColeccion(String titulo, String descripcion, List<Hecho> hechos,List<HechoStrategy> criteriosDePertenencia) {
        List<Hecho> hechosFiltrados = this.aplicarCriterios(criteriosDePertenencia,hechos);
        return this.coleccionRepository.create(titulo,descripcion,hechosFiltrados,criteriosDePertenencia);
    }

    private List<Hecho> aplicarCriterios(List<HechoStrategy> criteriosDePertenencia,List<Hecho> hechos){
        return hechos.stream()
                .filter(hecho -> criteriosDePertenencia.stream().allMatch(criterio -> criterio.cumple(hecho)))
                .collect(Collectors.toList());
    }

    public Coleccion obtenerColeccion(String handle){
        return this.coleccionRepository.read(handle);
    }
}

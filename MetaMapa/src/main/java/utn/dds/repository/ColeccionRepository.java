package utn.dds.repository;

import org.apache.commons.lang3.NotImplementedException;
import utn.dds.model.Coleccion;
import utn.dds.model.Hecho;
import utn.dds.model.criterios.HechoStrategy;
import java.util.List;

public class ColeccionRepository {

    public Coleccion create(String titulo, String descripcion, List<Hecho> hechos, List<HechoStrategy> criteriosDePertenencia){
        //TODO: Por ahora se instancia la clase y se devuelve, pero en un futuro hay que guardar en la DB
        return new Coleccion(titulo, descripcion, hechos, criteriosDePertenencia);
    }

    public Coleccion read(String handle){
        throw  new NotImplementedException("Metodo todavia NO implementado");
    }

    public Coleccion update(String handle){
        throw  new NotImplementedException("Metodo todavia NO implementado");
    }

    public void delete(String handle){
        throw new  NotImplementedException("Metodo todavia NO implementado");
    }
    
}

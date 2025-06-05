package utn.dds.service.negocio;

import utn.dds.model.Hecho;
import utn.dds.model.fuentes.estatica.FuenteEstatica;
import utn.dds.model.fuentes.estatica.strategies.CSVStrategy;
import utn.dds.model.fuentes.estatica.strategies.ProcesadorStrategy;

import java.nio.file.Path;
import java.util.List;

public class FuenteDeDatos {
    public List<Hecho> importarHechosDesdeCSV(Path path,String separador){
        ProcesadorStrategy procesador = new CSVStrategy(path,separador);
        FuenteEstatica fuente = new FuenteEstatica(procesador);
        return fuente.obtenerHechos();
    }
}

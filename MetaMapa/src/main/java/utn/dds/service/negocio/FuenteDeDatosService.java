package utn.dds.service.negocio;
import utn.dds.model.*;
import utn.dds.model.fuentes.FuenteDeDatos;
import utn.dds.model.fuentes.estatica.strategies.ProcesadorStrategy;
import utn.dds.model.fuentes.proxy.Conexion;
import utn.dds.model.fuentes.proxy.FuenteMetaMapa;
import utn.dds.model.fuentes.proxy.FuenteProxyDemo;
import utn.dds.repository.HechoRepository;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class FuenteDeDatosService {
    private final HechoRepository hechoRepository;

    public FuenteDeDatosService() {
        this.hechoRepository = new HechoRepository();
    }

    /*
    --- IMPORTS DESDE DISTINTAS FUENTES. ---
    */
    public List<Hecho>  importarDesdeEstatica(ProcesadorStrategy procesador){
       return this.hechoRepository.desdeEstatica(procesador);
    }

    public List<Hecho> importarDesdeDemo(Conexion conexion, URL url){
        FuenteDeDatos fuenteProxy = new FuenteProxyDemo(conexion,url);
        return this.hechoRepository.desdeProxy(fuenteProxy);
    }

    public List<Hecho> importarDesdeProxyMetaMapa(String handle){
        FuenteDeDatos fuenteMetaMapa = new FuenteMetaMapa(handle);
        return this.hechoRepository.desdeProxy(fuenteMetaMapa);
    }

    public List<Hecho> importarDesdeDinamica(){
        return List.of();
    }

    /*
    ---PERSISTENCIA DE HECHOS DINAMICOS---
    */
    public Hecho cargarHecho(Contribuyente contribuyente, String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                             Origen origen, TipoHecho tipo,
                             double longitud, double latitud, LocalDateTime fechaCarga,
                             EstadoHecho estado, List<String> etiquetas){
        return this.hechoRepository.persistirHecho(
                contribuyente, titulo, descripcion, categoria, fechaAcontecimiento,
                origen, tipo, longitud, latitud, fechaCarga, estado, etiquetas
        );
    }
}

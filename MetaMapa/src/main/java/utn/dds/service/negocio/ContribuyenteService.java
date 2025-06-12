package utn.dds.service.negocio;

import utn.dds.model.*;
import utn.dds.repository.HechoRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ContribuyenteService {

    private final FuenteDeDatosService fuenteDeDatosService;
    private final Contribuyente contribuyente;

    public ContribuyenteService(Contribuyente contribuyente){
        this.fuenteDeDatosService = new FuenteDeDatosService();
        this.contribuyente = contribuyente;
    }

    public Hecho aportarHecho(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                             TipoHecho tipo,
                              double longitud, double latitud, LocalDateTime fechaCarga,
                              EstadoHecho estado, List<String> etiquetas){
        return fuenteDeDatosService.cargarHecho(contribuyente, titulo, descripcion, categoria, fechaAcontecimiento,
                Origen.CONTRIBUYENTE, tipo, longitud, latitud, fechaCarga, estado, etiquetas);
    }
}

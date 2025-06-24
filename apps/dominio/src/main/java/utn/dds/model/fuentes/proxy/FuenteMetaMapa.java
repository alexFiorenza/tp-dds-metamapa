package utn.dds.model.fuentes.proxy;

import utn.dds.model.*;
import utn.dds.model.fuentes.FuenteDeDatos;
import utn.dds.model.fuentes.TipoFuente;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FuenteMetaMapa implements FuenteDeDatos {
    private final TipoFuente tipoFuente;
    private final String handle;
    
    public FuenteMetaMapa(String handle) {
        this.handle = handle;
        this.tipoFuente = TipoFuente.PROXY;
    }

    // Harcodeamos para probar
    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<Hecho>();
        LocalDateTime fechaCarga = LocalDateTime.now();
        List<String> etiquetas = List.of("urgente", "transporte");

        Contribuyente contribuyente = new Contribuyente("Pedro", "Ruiz", 22);

        Hecho hecho = new Hecho("Ataque en el bosque", "3 muertos",
                "Natural", LocalDate.now(), Origen.CONTRIBUYENTE,contribuyente, TipoHecho.TEXTO, 0, 0, fechaCarga,
                EstadoHecho.OCULTO, etiquetas);
        hechos.add(hecho);

        return hechos;
    }

    @Override
    public TipoFuente tipo() {
        return this.tipoFuente;
    }
} 
package utn.dds.service.externo;

import utn.dds.model.fuentes.proxy.FuenteMetaMapa;
import utn.dds.model.*;
import utn.dds.service.negocio.FuenteDeDatosService;

import java.util.List;

public class MetaMapaService {

    private final FuenteMetaMapa metaMapa;

    public MetaMapaService(String handle) {
        this.metaMapa = new FuenteMetaMapa(handle);
    }

    public List<Hecho> obtenerHechos() {
        return metaMapa.obtenerHechos();
    }
}

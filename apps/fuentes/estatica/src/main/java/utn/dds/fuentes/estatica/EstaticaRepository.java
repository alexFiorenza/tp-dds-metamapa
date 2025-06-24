package utn.dds.fuentes.estatica;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.estatica.FuenteEstatica;
import utn.dds.dominio.fuentes.estatica.strategies.ProcesadorStrategy;
import utn.dds.repository.FileSystem;
import utn.dds.repository.IDAO;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public class EstaticaRepository {
    private final IDAO<Hecho> dao;
    private final ProcesadorStrategy procesador;
    private FuenteDeDatos fuente;

    public EstaticaRepository(Path url, ProcesadorStrategy procesador) {
        this.procesador = procesador;
        this.dao = new FileSystem<>(url);
    }

    public List<Hecho> obtenerHechos() throws IOException {
        InputStream data = dao.read();
        this.fuente = new FuenteEstatica(data, procesador);
        return fuente.obtenerHechos();
    }
} 
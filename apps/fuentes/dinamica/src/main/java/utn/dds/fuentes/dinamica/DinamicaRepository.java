package utn.dds.fuentes.dinamica;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.FuenteDinamica;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class DinamicaRepository {
    private final IDAO<Hecho> dao;
    private FuenteDeDatos fuente;

    public DinamicaRepository(IDAO<Hecho> dao) {
        this.dao = dao;
    }

    // Hay que codear las funciones, esto seria lo ultimo

    public List<Hecho> obtenerHechos() throws IOException {
        InputStream data = dao.read();
        return fuente.obtenerHechos();
    }

    public List<Hecho> aportarHechos(List<Hecho> hechos) throws IOException {
        return ;
    }

    public List<SolicitudEliminacion> obtenerSolicitudes() throws IOException {
        return ;
    }

    public List<SolicitudEliminacion> agregarSolicitudes(SolicitudEliminacion agregarSolicitud) throws IOException {
        return ;
    }
} 
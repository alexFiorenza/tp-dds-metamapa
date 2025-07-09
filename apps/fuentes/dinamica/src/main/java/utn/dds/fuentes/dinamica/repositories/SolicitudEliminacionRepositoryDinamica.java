package utn.dds.fuentes.dinamica.repositories;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.fuentes.FuenteDeDatos;

import java.io.IOException;
import java.util.List;

public class SolicitudEliminacionRepositoryDinamica {
    private final IDAO<Hecho> dao;
    private FuenteDeDatos fuente;

    public SolicitudEliminacionRepositoryDinamica(IDAO<Hecho> dao){ this.dao = dao; }

    public List<SolicitudEliminacion> obtenerSolicitudes(String uuid){
        return null;
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
        return null;
    }

    public void aceptarSolicitud(String uuid){

    }

    public void rechazarSolicitud(String uuid){

    }
}

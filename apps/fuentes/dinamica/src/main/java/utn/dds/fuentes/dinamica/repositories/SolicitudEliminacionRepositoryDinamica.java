package utn.dds.fuentes.dinamica.repositories;

import utn.dds.daos.IDAO;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.SolicitudEliminacionDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

public class SolicitudEliminacionRepositoryDinamica {
    private final IDAO<SolicitudEliminacion> dao;
    private FuenteDeDatos fuente;

    public SolicitudEliminacionRepositoryDinamica(IDAO<SolicitudEliminacion> dao){ this.dao = dao; }

    public List<SolicitudEliminacion> obtenerSolicitudes(){
        List<SolicitudEliminacion> solicitudes = dao.find();
        return solicitudes;
    }

    public SolicitudEliminacion obtenerSolicitud(String uuid) throws IOException{
        List<SolicitudEliminacion> solicitudes = obtenerSolicitudes();

        Optional<SolicitudEliminacion> encontrada = solicitudes.stream()
                .filter(s -> s.getUuid().equals(uuid))
                .findFirst();

        if (encontrada.isPresent()) {
            SolicitudEliminacion solicitud = encontrada.get();
            return solicitud;
        } else {
            throw new NoSuchElementException("No se encontró una solicitud con UUID: " + uuid);
        }
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
        dao.save(solicitud);
        return solicitud;
    }

    public SolicitudEliminacion procesarSolicitud(String uuid) throws IOException{
        SolicitudEliminacion solicitud = obtenerSolicitud(uuid);
        solicitud.ocultar();
        dao.save(solicitud);
        return solicitud;
    }
}

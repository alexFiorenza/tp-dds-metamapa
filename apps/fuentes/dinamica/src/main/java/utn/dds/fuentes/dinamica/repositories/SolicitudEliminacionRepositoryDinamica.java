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
    private final IDAO<SolicitudEliminacionDTO> dao;
    private FuenteDeDatos fuente;

    public SolicitudEliminacionRepositoryDinamica(IDAO<SolicitudEliminacionDTO> dao){ this.dao = dao; }

    public List<SolicitudEliminacion> obtenerSolicitudes(){
        List<SolicitudEliminacionDTO> solicitudesDTO = dao.find();
        return solicitudesDTO.stream()
                .map(SolicitudEliminacionDTO::toSolicitudEliminacion)
                .collect(Collectors.toList());
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
            SolicitudEliminacionDTO solicitudDTO = new SolicitudEliminacionDTO(solicitud.getTexto(), solicitud.getHecho(),
                                                    solicitud.getFechaSolicitud(), solicitud.getEstado(), solicitud.getUuid());
            dao.save(solicitudDTO);
        return solicitud;
    }


    public SolicitudEliminacion aceptarSolicitud(String uuid) throws IOException{
        SolicitudEliminacion solicitud = obtenerSolicitud(uuid);
        solicitud.ocultar();

        SolicitudEliminacionDTO solicitudDTO = new SolicitudEliminacionDTO(solicitud.getTexto(), solicitud.getHecho(),
                solicitud.getFechaSolicitud(), solicitud.getEstado(), solicitud.getUuid());
        dao.save(solicitudDTO);

        return solicitud;
    }

    public SolicitudEliminacion rechazarSolicitud(String uuid) throws IOException{
        SolicitudEliminacion solicitud = obtenerSolicitud(uuid);
        solicitud.ocultar();

        SolicitudEliminacionDTO solicitudDTO = new SolicitudEliminacionDTO(solicitud.getTexto(), solicitud.getHecho(),
                solicitud.getFechaSolicitud(), solicitud.getEstado(), solicitud.getUuid());
        dao.save(solicitudDTO);

        return solicitud;
    }
}

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

    public List<SolicitudEliminacion> obtenerSolicitud(String uuid){
        return null;
    }

    public SolicitudEliminacion agregarSolicitud(SolicitudEliminacion solicitud) throws IOException {
            SolicitudEliminacionDTO solicitudDTO = new SolicitudEliminacionDTO(solicitud.getTexto(), solicitud.getHecho(),
                                                    solicitud.getFechaSolicitud(), solicitud.getEstado(), solicitud.getUuid());
            dao.save(solicitudDTO);
        return solicitud;
    }


    public void aceptarSolicitud(String uuid){
    // paso a paso
        // buscar en la dao la solicitud == uuid
        //cambiarEstado
        // ver que hacer con esa solicitud
    }

    public void rechazarSolicitud(String uuid){
        // paso a paso
        // buscar en la dao la solicitud == uuid
        //cambiarEstado
        // ver que hacer con esa solicitud
    }
}

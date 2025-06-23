package utn.dds.service.negocio;

import utn.dds.model.DetectorSpam;
import utn.dds.model.EstadoSolicitud;
import utn.dds.model.SolicitudEliminacion;
import utn.dds.repository.SolicitudEliminacionRepository;

public class SolicitudEliminacionService {
    private final SolicitudEliminacionRepository solicitudEliminacionRepository;
    private final DetectorSpam detectorSpam;

    public SolicitudEliminacionService(DetectorSpam detectorSpam) {
        solicitudEliminacionRepository = new SolicitudEliminacionRepository();
        this.detectorSpam = detectorSpam;
    }

    public SolicitudEliminacion solicitarEliminacion(String uuidHecho, String texto) {
        SolicitudEliminacion solicitud;
        try{
            if(detectorSpam.esSpam(texto)){
                solicitud = this.solicitudEliminacionRepository.create(uuidHecho, texto, EstadoSolicitud.OCULTO);
            }else{
                solicitud = this.solicitudEliminacionRepository.create(uuidHecho, texto, EstadoSolicitud.ACTIVO);
            }
            return solicitud;
        } catch(Exception e){
            throw  new RuntimeException(e);
        }
    }

    public SolicitudEliminacion aceptarSolicitud(String _uuidSolicitud){
        return this.solicitudEliminacionRepository.update(_uuidSolicitud,EstadoSolicitud.ACTIVO);
    }

    public SolicitudEliminacion rechazarSolicitud(String _uuidSolicitud){
        return this.solicitudEliminacionRepository.update(_uuidSolicitud,EstadoSolicitud.OCULTO);
    }
}

package utn.dds.repository;
import utn.dds.model.EstadoSolicitud;
import utn.dds.model.SolicitudEliminacion;

import java.time.LocalDateTime;
import java.util.UUID;

public class SolicitudEliminacionRepository {
    private HechoRepository hechoRepository;

    public SolicitudEliminacionRepository() {
        this.hechoRepository = new  HechoRepository();
    }

    public SolicitudEliminacion create(
            String _uuidHecho, String texto,
            EstadoSolicitud estadoSolicitud
    ) {
        //TODO:  Almacenar en la Base de Datos
        // Sino encuentra el hecho que tiene que eliminar throw new NotFoundException("No existe el hecho que se quiere eliminar");
        // OJO: El objeto de bases de datos no tiene que tener el detector de spam xq no le importa (Es logica de negocio)
        String uuid = UUID.randomUUID().toString(); // Por ahora lo creo aca pero tiene que venir de la BD.
        return new SolicitudEliminacion(
                texto,
                _uuidHecho,
                LocalDateTime.now(),
                estadoSolicitud,
                uuid
        );
    }

    public SolicitudEliminacion update(String _uuid, EstadoSolicitud nuevoEstado){
        if(nuevoEstado == EstadoSolicitud.OCULTO){
            return this.rechazarSolicitudEliminacion(_uuid);
        }else{
            return this.aceptarSolicitudEliminacion(_uuid);
        }
    }

    private SolicitudEliminacion aceptarSolicitudEliminacion(String _uuid){
        // TODO: Proceso de Bases de Datos
        // 1. Obtener Solicitud de eliminacion desde la  Base de Datos -> solicitud =  orm.getById("solicitud",_uuid);
        // 2. Actualizar el estado del hecho usando su Repository -> hechoRepository.ocultar(solicitud.uuidHecho);
        // Por ahora devuelvo una solicitud de eliminacion mock

        return new SolicitudEliminacion(
                "Esto es una solicitud de prueba",
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                EstadoSolicitud.ACTIVO,
                _uuid
        );
    }

    private SolicitudEliminacion rechazarSolicitudEliminacion(String _uuid){
        // TODO: Proceso de Bases de Datos
        // 1. Obtener Solicitud de eliminacion desde la  Base de Datos -> solicitud =  orm.getById("solicitud",_uuid);
        // 2. Cambiamos el estado de la solicitud a oculta (ya que se rechazo) -> orm.patch("solicitud",_uuid,{"Estado":"OCULTO"})
        // Por ahora devuelvo una solicitud de eliminacion mock

        return new SolicitudEliminacion(
                "Esto es una solicitud de prueba",
                UUID.randomUUID().toString(),
                LocalDateTime.now(),
                EstadoSolicitud.OCULTO,
                _uuid
        );
    }
}

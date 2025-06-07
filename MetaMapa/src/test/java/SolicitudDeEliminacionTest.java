import org.junit.jupiter.api.Test;
import utn.dds.model.AlgoritmoTFIDF;
import utn.dds.model.EstadoSolicitud;
import utn.dds.model.SolicitudEliminacion;
import utn.dds.service.negocio.SolicitudEliminacionService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SolicitudDeEliminacionTest {
    /**
     * Como persona visualizadora,
     * deseo poder solicitar la eliminación de un hecho.
     */
    @Test
    void solicitarEliminacionDeHecho(){
        SolicitudEliminacionService solicitudEliminacionService = new SolicitudEliminacionService(new AlgoritmoTFIDF());
        SolicitudEliminacion solicitud = solicitudEliminacionService.solicitarEliminacion(
                UUID.randomUUID().toString(),
                "Esto es un test de eliminacion"
        );
        assertNotNull(solicitud,"Se creo una solicitud de eliminacion");
    }

    /**
     * Como persona administradora,
     * deseo poder aceptar  la solicitud de eliminación de un
     * hecho.
     */
    @Test
    void aceptarSolicitudEliminacion(){
        SolicitudEliminacionService solicitudEliminacionService = new SolicitudEliminacionService(new AlgoritmoTFIDF());
        SolicitudEliminacion solicitud = solicitudEliminacionService.aceptarSolicitud(UUID.randomUUID().toString());
        assertNotNull(solicitud,"Se actualizo el estado de su solicitud de eliminacion");
    }

    /**
     * Como persona administradora,
     * deseo poder aceptar  la solicitud de eliminación de un
     * hecho.
     */
    @Test
    void rechazarSolicitudEliminacion(){
        SolicitudEliminacionService solicitudEliminacionService = new SolicitudEliminacionService(new AlgoritmoTFIDF());
        SolicitudEliminacion solicitud = solicitudEliminacionService.rechazarSolicitud(UUID.randomUUID().toString());
        assertNotNull(solicitud,"Se actualizo el estado de su solicitud de eliminacion");
        assertEquals(EstadoSolicitud.OCULTO, solicitud.getEstado());
    }
}

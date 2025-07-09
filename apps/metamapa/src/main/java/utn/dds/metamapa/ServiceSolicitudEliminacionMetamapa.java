package utn.dds.metamapa;

import utn.dds.dominio.SolicitudEliminacion;

public class ServiceSolicitudEliminacionMetamapa {
    private final RepositorySolicitudEliminacion repositorySolicitudEliminacion;
    public ServiceSolicitudEliminacionMetamapa(){
        this.repositorySolicitudEliminacion=new RepositorySolicitudEliminacion();
    }
    public void crearSolicitud(SolicitudEliminacion solicitud) {
        repositorySolicitudEliminacion.create();
    }
}

package utn.dds.dominio.criterios;

import utn.dds.dominio.Hecho;
import java.util.Objects;

public class EstrategiaTituloUbicacionFecha implements EstrategiaDeteccionDuplicados {
    
    private static final double TOLERANCIA_UBICACION = 0.0001; // Aproximadamente 10 metros
    
    @Override
    public boolean esDuplicado(Hecho hecho1, Hecho hecho2) {
        if (hecho1 == null || hecho2 == null) {
            return false;
        }
        
        return sonTitulosIguales(hecho1, hecho2) 
            && sonUbicacionesIguales(hecho1, hecho2)
            && sonFechasIguales(hecho1, hecho2);
    }
    
    private boolean sonTitulosIguales(Hecho hecho1, Hecho hecho2) {
        return Objects.equals(
            normalizarTitulo(hecho1.getTitulo()), 
            normalizarTitulo(hecho2.getTitulo())
        );
    }
    
    private String normalizarTitulo(String titulo) {
        if (titulo == null) {
            return null;
        }
        return titulo.trim().toLowerCase();
    }
    
    private boolean sonUbicacionesIguales(Hecho hecho1, Hecho hecho2) {
        return Math.abs(hecho1.getLatitud() - hecho2.getLatitud()) <= TOLERANCIA_UBICACION
            && Math.abs(hecho1.getLongitud() - hecho2.getLongitud()) <= TOLERANCIA_UBICACION;
    }
    
    private boolean sonFechasIguales(Hecho hecho1, Hecho hecho2) {
        return Objects.equals(hecho1.getFechaAcontecimiento(), hecho2.getFechaAcontecimiento());
    }
}
package utn.dds.mappers;

import utn.dds.dominio.SolicitudEliminacion;
import utn.dds.jpa.entities.SolicitudEliminacionEntity;

public class SolicitudEliminacionMapper {

    public static SolicitudEliminacion toDomain(SolicitudEliminacionEntity entity) {
        if (entity == null) {
            return null;
        }

        return new SolicitudEliminacion(
            entity.getTexto(),
            entity.getHecho(),
            entity.getFechaSolicitud(),
            entity.getEstado(),
            entity.getUuid()
        );
    }

    public static SolicitudEliminacionEntity toEntity(SolicitudEliminacion domain) {
        if (domain == null) {
            return null;
        }

        SolicitudEliminacionEntity entity = new SolicitudEliminacionEntity();
        entity.setUuid(domain.getUuid());
        entity.setTexto(domain.getTexto());
        entity.setHecho(domain.getHecho());
        entity.setFechaSolicitud(domain.getFechaSolicitud());
        entity.setEstado(domain.getEstado());

        return entity;
    }
}
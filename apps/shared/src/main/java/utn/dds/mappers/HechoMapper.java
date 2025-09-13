package utn.dds.mappers;

import utn.dds.dominio.Hecho;
import utn.dds.jpa.entities.HechoEntity;

public class HechoMapper {

    public static Hecho toDomain(HechoEntity entity) {
        if (entity == null) {
            return null;
        }

        Hecho hecho = new Hecho();
        hecho.setUuid(entity.getUuid());
        hecho.setTitulo(entity.getTitulo());
        hecho.setDescripcion(entity.getDescripcion());
        hecho.setCategoria(entity.getCategoria());
        hecho.setFechaAcontecimiento(entity.getFechaAcontecimiento());
        hecho.setOrigen(entity.getOrigen());
        hecho.setContribuyente(ContribuyenteMapper.toDomain(entity.getContribuyente()));
        hecho.setTipo(entity.getTipo());
        hecho.setLongitud(entity.getLongitud());
        hecho.setLatitud(entity.getLatitud());
        hecho.setFechaCarga(entity.getFechaCarga());
        hecho.setEstado(entity.getEstado());
        hecho.setEtiquetas(entity.getEtiquetas());

        return hecho;
    }

    public static HechoEntity toEntity(Hecho domain) {
        if (domain == null) {
            return null;
        }

        HechoEntity entity = new HechoEntity();
        entity.setUuid(domain.getUuid());
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());
        entity.setCategoria(domain.getCategoria());
        entity.setFechaAcontecimiento(domain.getFechaAcontecimiento());
        entity.setOrigen(domain.getOrigen());
        entity.setContribuyente(ContribuyenteMapper.toEntity(domain.getContribuyente()));
        entity.setTipo(domain.getTipo());
        entity.setLongitud(domain.getLongitud());
        entity.setLatitud(domain.getLatitud());
        entity.setFechaCarga(domain.getFechaCarga());
        entity.setEstado(domain.getEstado());
        entity.setEtiquetas(domain.getEtiquetas());

        return entity;
    }
}
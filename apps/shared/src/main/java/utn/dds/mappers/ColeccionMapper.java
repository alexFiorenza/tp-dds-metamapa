package utn.dds.mappers;

import utn.dds.dominio.Coleccion;
import utn.dds.jpa.entities.ColeccionEntity;

import java.util.stream.Collectors;

public class ColeccionMapper {

    public static Coleccion toDomain(ColeccionEntity entity) {
        if (entity == null) {
            return null;
        }

        Coleccion coleccion = new Coleccion();
        coleccion.setHandle(entity.getHandle());
        coleccion.setTitulo(entity.getTitulo());
        coleccion.setDescripcion(entity.getDescripcion());

        // Convertir hechos de entity a domain
        if (entity.getHechos() != null) {
            coleccion.setHechos(
                entity.getHechos().stream()
                    .map(HechoMapper::toDomain)
                    .collect(Collectors.toList())
            );
        }

        // Los criteriosDePertenencia no se persisten, se inicializan vacíos
        // Se pueden cargar/configurar por separado según la lógica de negocio

        return coleccion;
    }

    public static ColeccionEntity toEntity(Coleccion domain) {
        if (domain == null) {
            return null;
        }

        ColeccionEntity entity = new ColeccionEntity();
        entity.setHandle(domain.getHandle());
        entity.setTitulo(domain.getTitulo());
        entity.setDescripcion(domain.getDescripcion());

        // Convertir hechos de domain a entity
        if (domain.getHechos() != null) {
            entity.setHechos(
                domain.getHechos().stream()
                    .map(HechoMapper::toEntity)
                    .collect(Collectors.toList())
            );
        }

        // Los criteriosDePertenencia no se persisten

        return entity;
    }
}
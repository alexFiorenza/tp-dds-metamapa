package utn.dds.mappers;

import utn.dds.dominio.Coleccion;
import utn.dds.jpa.entities.ColeccionEntity;
import utn.dds.dto.ColeccionCreateDTO;
import utn.dds.dto.ColeccionDTO;
import utn.dds.dto.FuenteDTO;
import utn.dds.dto.CriterioCreateDTO;
import utn.dds.dominio.criterios.HechoStrategy;

import java.util.stream.Collectors;
import java.util.UUID;

public class ColeccionMapper {

    public static Coleccion toDomain(ColeccionEntity entity) {
        if (entity == null) {
            return null;
        }

        Coleccion coleccion = new Coleccion();
        coleccion.setHandle(entity.getHandle());
        coleccion.setTitulo(entity.getTitulo());
        coleccion.setDescripcion(entity.getDescripcion());

        // NO convertir hechos aquí - se mantienen lazy para carga bajo demanda
        // Se inicializa con lista vacía
        coleccion.setHechos(new java.util.ArrayList<>());

        // Convertir criterios de pertenencia de entity a domain (están cargados con JOIN FETCH)
        if (entity.getCriteriosDePertenencia() != null) {
            coleccion.setCriteriosDePertenencia(
                CriterioMapper.toDomainList(entity.getCriteriosDePertenencia())
            );
        }

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

        // Convertir criterios de pertenencia de domain a entity
        if (domain.getCriteriosDePertenencia() != null) {
            entity.setCriteriosDePertenencia(
                CriterioMapper.toEntityList(domain.getCriteriosDePertenencia(), domain.getHandle())
            );
        }

        return entity;
    }

    public static Coleccion fromCreateDTO(ColeccionCreateDTO dto) {
        if (dto == null) {
            return null;
        }

        Coleccion coleccion = new Coleccion();
        coleccion.setTitulo(dto.getTitulo());
        coleccion.setDescripcion(dto.getDescripcion());

        // Convertir criterios de DTO a domain
        if (dto.getCriteriosDePertenencia() != null) {
            java.util.List<HechoStrategy> criterios = dto.getCriteriosDePertenencia().stream()
                    .map(criterioDTO -> criterioDTO.toHechoStrategy())
                    .collect(Collectors.toList());
            coleccion.setCriteriosDePertenencia(criterios);
        }

        // Los hechos se asociarán automáticamente según los criterios
        // Las fuentes se manejarán en el servicio usando dto.getFuentesIds()

        return coleccion;
    }

    public static ColeccionDTO toDTO(ColeccionEntity entity) {
        if (entity == null) {
            return null;
        }

        ColeccionDTO dto = new ColeccionDTO();
        dto.setHandle(entity.getHandle());
        dto.setTitulo(entity.getTitulo());
        dto.setDescripcion(entity.getDescripcion());

        // Mapear fuentes (cargadas con JOIN FETCH)
        if (entity.getFuentes() != null) {
            dto.setFuentes(
                entity.getFuentes().stream()
                    .map(fuenteEntity -> new FuenteDTO(
                        fuenteEntity.getHost(),
                        fuenteEntity.getParams(),
                        UUID.fromString(fuenteEntity.getUuid())
                    ))
                    .collect(Collectors.toList())
            );
        } else {
            dto.setFuentes(new java.util.ArrayList<>());
        }

        // Mapear criterios (cargados con JOIN FETCH)
        if (entity.getCriteriosDePertenencia() != null) {
            dto.setCriteriosDePertenencia(
                entity.getCriteriosDePertenencia().stream()
                    .map(criterioEntity -> {
                        CriterioCreateDTO criterioDTO = new CriterioCreateDTO();
                        criterioDTO.setTipo(criterioEntity.getTipo());
                        criterioDTO.setEstado(criterioEntity.getEstado());
                        return criterioDTO;
                    })
                    .collect(Collectors.toList())
            );
        } else {
            dto.setCriteriosDePertenencia(new java.util.ArrayList<>());
        }

        return dto;
    }
}
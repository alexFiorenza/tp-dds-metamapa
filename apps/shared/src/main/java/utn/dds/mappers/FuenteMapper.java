package utn.dds.mappers;

import utn.dds.dto.FuenteDTO;
import utn.dds.jpa.entities.FuenteEntity;
import java.util.UUID;

public class FuenteMapper {

    public static FuenteDTO toDomain(FuenteEntity entity) {
        if (entity == null) {
            return null;
        }

        FuenteDTO dto = new FuenteDTO();
        dto.setUuid(UUID.fromString(entity.getUuid()));
        dto.setHost(entity.getHost());

        // Directamente asignar params ya que son Map<String, Object>
        dto.setParams(entity.getParams());

        return dto;
    }


    public static FuenteEntity toEntity(FuenteDTO dto) {
        if (dto == null) {
            return null;
        }

        FuenteEntity entity = new FuenteEntity();
        if (dto.getUuid() != null) {
            entity.setUuid(dto.getUuid().toString());
        }
        entity.setHost(dto.getHost());

        // Directamente asignar params ya que son Map<String, Object>
        entity.setParams(dto.getParams());

        return entity;
    }

}
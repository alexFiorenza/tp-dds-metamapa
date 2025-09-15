package utn.dds.mappers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import utn.dds.dto.FuenteDTO;
import utn.dds.jpa.entities.FuenteEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FuenteMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static FuenteDTO toDomain(FuenteEntity entity) {
        if (entity == null) {
            return null;
        }

        FuenteDTO dto = new FuenteDTO();
        dto.setUuid(UUID.fromString(entity.getUuid()));
        dto.setHost(entity.getHost());

        // Convertir Map<String, String> a Map<String, Object> con parsing inteligente
        if (entity.getParams() != null) {
            Map<String, Object> objectParams = new HashMap<>();
            for (Map.Entry<String, String> entry : entity.getParams().entrySet()) {
                objectParams.put(entry.getKey(), parseValue(entry.getValue()));
            }
            dto.setParams(objectParams);
        }

        return dto;
    }

    private static Object parseValue(String value) {
        if (value == null) {
            return null;
        }

        // Intentar parsear como JSON si el string parece ser un objeto o array
        if ((value.startsWith("{") && value.endsWith("}")) ||
            (value.startsWith("[") && value.endsWith("]"))) {
            try {
                return objectMapper.readValue(value, Object.class);
            } catch (Exception e) {
                // Si falla el parsing JSON, devolver como string
                return value;
            }
        }

        // Para strings que parecen mapas simples como "{key=value, key2=value2}"
        if (value.startsWith("{") && value.endsWith("}") && value.contains("=")) {
            try {
                return parseSimpleMapString(value);
            } catch (Exception e) {
                return value;
            }
        }

        // Si no es JSON, devolver como string
        return value;
    }

    private static Map<String, Object> parseSimpleMapString(String mapStr) {
        Map<String, Object> result = new HashMap<>();
        // Remover { y }
        String content = mapStr.substring(1, mapStr.length() - 1);

        // Dividir por comas
        String[] pairs = content.split(",\\s*");

        for (String pair : pairs) {
            String[] keyValue = pair.split("=", 2);
            if (keyValue.length == 2) {
                result.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }

        return result;
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

        // Convertir Map<String, Object> a Map<String, String> con serialización inteligente
        if (dto.getParams() != null) {
            Map<String, String> stringParams = new HashMap<>();
            for (Map.Entry<String, Object> entry : dto.getParams().entrySet()) {
                stringParams.put(entry.getKey(), serializeValue(entry.getValue()));
            }
            entity.setParams(stringParams);
        }

        return entity;
    }

    private static String serializeValue(Object value) {
        if (value == null) {
            return null;
        }

        // Si es un Map o una List, serializar como JSON
        if (value instanceof Map || value instanceof java.util.List) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                return value.toString();
            }
        }

        // Para otros tipos, usar toString
        return value.toString();
    }
}
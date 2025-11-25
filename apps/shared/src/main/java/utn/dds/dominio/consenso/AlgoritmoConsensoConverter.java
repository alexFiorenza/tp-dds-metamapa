package utn.dds.dominio.consenso;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converter de JPA para convertir entre String (persistido en BD) y AlgoritmoConsenso (interfaz).
 * Permite persistir el tipo como String y cargar la instancia correcta de la interfaz.
 */
@Converter(autoApply = true)
public class AlgoritmoConsensoConverter implements AttributeConverter<AlgoritmoConsenso, String> {

    @Override
    public String convertToDatabaseColumn(AlgoritmoConsenso algoritmo) {
        if (algoritmo == null) {
            return "default";
        }
        return AlgoritmoConsensoFactory.obtenerTipo(algoritmo);
    }

    @Override
    public AlgoritmoConsenso convertToEntityAttribute(String tipo) {
        return AlgoritmoConsensoFactory.crear(tipo);
    }
}


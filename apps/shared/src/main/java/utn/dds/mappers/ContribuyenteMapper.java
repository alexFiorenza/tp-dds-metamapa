package utn.dds.mappers;

import utn.dds.dominio.Contribuyente;
import utn.dds.jpa.entities.ContribuyenteEntity;

import java.util.stream.Collectors;

public class ContribuyenteMapper {

    public static Contribuyente toDomain(ContribuyenteEntity entity) {
        if (entity == null) {
            return null;
        }

        Contribuyente contribuyente = new Contribuyente();
        contribuyente.setId(entity.getId());
        contribuyente.setNombre(entity.getNombre());
        contribuyente.setApellido(entity.getApellido());
        contribuyente.setEdad(entity.getEdad());

        // Evitamos conversión circular de aportes por lazy loading
        // Los aportes se cargarán por separado cuando sea necesario

        return contribuyente;
    }

    public static ContribuyenteEntity toEntity(Contribuyente domain) {
        if (domain == null) {
            return null;
        }

        ContribuyenteEntity entity = new ContribuyenteEntity();
        entity.setId(domain.getId());
        entity.setNombre(domain.getNombre());
        entity.setApellido(domain.getApellido());
        entity.setEdad(domain.getEdad());

        // Evitamos conversión circular de aportes
        // Los aportes se manejan por separado

        return entity;
    }
}
package utn.dds.mappers;

import utn.dds.dominio.criterios.*;
import utn.dds.dominio.EstadoHecho;
import utn.dds.jpa.entities.CriterioEntity;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CriterioMapper {

    public static CriterioEntity toEntity(HechoStrategy strategy, String idColeccion) {
        if (strategy == null) {
            return null;
        }

        CriterioEntity entity = new CriterioEntity(idColeccion, getTipoFromStrategy(strategy));

        if (strategy instanceof TituloStrategy) {
            TituloStrategy ts = (TituloStrategy) strategy;
            entity.setTitulo(ts.getTitulo());
        }
        else if (strategy instanceof CategoriaStrategy) {
            CategoriaStrategy cs = (CategoriaStrategy) strategy;
            entity.setCategoria(cs.getCategoria());
        }
        else if (strategy instanceof DescripcionStrategy) {
            DescripcionStrategy ds = (DescripcionStrategy) strategy;
            entity.setDescripcion(ds.getDescripcion());
        }
        else if (strategy instanceof OrigenStrategy) {
            OrigenStrategy os = (OrigenStrategy) strategy;
            entity.setOrigen(os.getOrigen());
        }
        else if (strategy instanceof FechaAcontecimientoStrategy) {
            FechaAcontecimientoStrategy fas = (FechaAcontecimientoStrategy) strategy;
            entity.setFechaAcontecimiento(fas.getFechaAcontecimiento());
        }
        else if (strategy instanceof LongitudStrategy) {
            LongitudStrategy ls = (LongitudStrategy) strategy;
            entity.setLongitud(ls.getLongitud());
        }
        else if (strategy instanceof LatitudStrategy) {
            LatitudStrategy ls = (LatitudStrategy) strategy;
            entity.setLatitud(ls.getLatitud());
        }
        else if (strategy instanceof EstadoStrategy) {
            EstadoStrategy es = (EstadoStrategy) strategy;
            entity.setEstado(es.getEstado().toString());
        }
        else if (strategy instanceof FechaCargaStrategy) {
            FechaCargaStrategy fcs = (FechaCargaStrategy) strategy;
            entity.setFechaCarga(fcs.getFechaCarga());
        }
        else if (strategy instanceof EtiquetasStrategy) {
            EtiquetasStrategy es = (EtiquetasStrategy) strategy;
            String etiquetasStr = String.join(",", es.getEtiquetas());
            entity.setEtiquetas(etiquetasStr);
        }

        return entity;
    }

    public static HechoStrategy toDomain(CriterioEntity entity) {
        if (entity == null) {
            return null;
        }

        String tipo = entity.getTipo();

        if ("titulo".equals(tipo)) {
            return new TituloStrategy(entity.getTitulo());
        }
        else if ("categoria".equals(tipo)) {
            return new CategoriaStrategy(entity.getCategoria());
        }
        else if ("descripcion".equals(tipo)) {
            return new DescripcionStrategy(entity.getDescripcion());
        }
        else if ("origen".equals(tipo)) {
            return new OrigenStrategy(entity.getOrigen());
        }
        else if ("fechaAcontecimiento".equals(tipo)) {
            return new FechaAcontecimientoStrategy(entity.getFechaAcontecimiento());
        }
        else if ("longitud".equals(tipo)) {
            return new LongitudStrategy(entity.getLongitud());
        }
        else if ("latitud".equals(tipo)) {
            return new LatitudStrategy(entity.getLatitud());
        }
        else if ("estado".equals(tipo)) {
            EstadoHecho estado = EstadoHecho.valueOf(entity.getEstado());
            return new EstadoStrategy(estado);
        }
        else if ("fechaCarga".equals(tipo)) {
            return new FechaCargaStrategy(entity.getFechaCarga());
        }
        else if ("etiquetas".equals(tipo)) {
            List<String> etiquetas = Arrays.stream(entity.getEtiquetas().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            return new EtiquetasStrategy(etiquetas);
        }

        throw new IllegalArgumentException("Tipo de criterio no soportado: " + tipo);
    }

    private static String getTipoFromStrategy(HechoStrategy strategy) {
        if (strategy instanceof TituloStrategy) return "titulo";
        if (strategy instanceof CategoriaStrategy) return "categoria";
        if (strategy instanceof DescripcionStrategy) return "descripcion";
        if (strategy instanceof OrigenStrategy) return "origen";
        if (strategy instanceof FechaAcontecimientoStrategy) return "fechaAcontecimiento";
        if (strategy instanceof LongitudStrategy) return "longitud";
        if (strategy instanceof LatitudStrategy) return "latitud";
        if (strategy instanceof EstadoStrategy) return "estado";
        if (strategy instanceof FechaCargaStrategy) return "fechaCarga";
        if (strategy instanceof EtiquetasStrategy) return "etiquetas";

        throw new IllegalArgumentException("Strategy no soportado: " + strategy.getClass().getSimpleName());
    }

    public static List<CriterioEntity> toEntityList(List<HechoStrategy> strategies, String idColeccion) {
        return strategies.stream()
                .map(strategy -> toEntity(strategy, idColeccion))
                .collect(Collectors.toList());
    }

    public static List<HechoStrategy> toDomainList(List<CriterioEntity> entities) {
        return entities.stream()
                .map(CriterioMapper::toDomain)
                .collect(Collectors.toList());
    }
}
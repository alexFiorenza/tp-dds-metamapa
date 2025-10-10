package utn.dds.metamapa.persistencia;

import utn.dds.dominio.criterios.*;
import utn.dds.dominio.Hecho;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;

import java.util.List;
import java.util.ArrayList;

public class StrategyToSQLAdapter {

    public static List<Predicate> convertirStrategiasASQL(List<HechoStrategy> strategies,
                                                          CriteriaBuilder cb,
                                                          Root<Hecho> root) {
        List<Predicate> predicates = new ArrayList<>();

        for (HechoStrategy strategy : strategies) {
            Predicate predicate = convertirEstrategiaASQL(strategy, cb, root);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }

        return predicates;
    }

    private static Predicate convertirEstrategiaASQL(HechoStrategy strategy,
                                                    CriteriaBuilder cb,
                                                    Root<Hecho> root) {

        if (strategy instanceof TituloStrategy) {
            TituloStrategy ts = (TituloStrategy) strategy;
            return cb.like(cb.lower(root.get("titulo")),
                          "%" + ts.getTitulo().toLowerCase() + "%");
        }

        if (strategy instanceof CategoriaStrategy) {
            CategoriaStrategy cs = (CategoriaStrategy) strategy;
            return cb.like(cb.lower(root.get("categoria")),
                          "%" + cs.getCategoria().toLowerCase() + "%");
        }

        if (strategy instanceof DescripcionStrategy) {
            DescripcionStrategy ds = (DescripcionStrategy) strategy;
            return cb.like(cb.lower(root.get("descripcion")),
                          "%" + ds.getDescripcion().toLowerCase() + "%");
        }

        if (strategy instanceof OrigenStrategy) {
            OrigenStrategy os = (OrigenStrategy) strategy;
            return cb.equal(root.get("origen"), os.getOrigen());
        }

        if (strategy instanceof FechaAcontecimientoStrategy) {
            FechaAcontecimientoStrategy fas = (FechaAcontecimientoStrategy) strategy;
            return cb.equal(root.get("fechaAcontecimiento"), fas.getFechaAcontecimiento());
        }

        if (strategy instanceof LongitudStrategy) {
            LongitudStrategy ls = (LongitudStrategy) strategy;
            return cb.equal(root.get("longitud"), ls.getLongitud());
        }

        if (strategy instanceof LatitudStrategy) {
            LatitudStrategy ls = (LatitudStrategy) strategy;
            return cb.equal(root.get("latitud"), ls.getLatitud());
        }

        if (strategy instanceof EstadoStrategy) {
            EstadoStrategy es = (EstadoStrategy) strategy;
            return cb.equal(root.get("estado"), es.getEstado());
        }

        if (strategy instanceof FechaCargaStrategy) {
            FechaCargaStrategy fcs = (FechaCargaStrategy) strategy;
            return cb.equal(root.get("fechaCarga"), fcs.getFechaCarga());
        }

        if (strategy instanceof EtiquetasStrategy) {
            EtiquetasStrategy es = (EtiquetasStrategy) strategy;

            // Hacemos JOIN con la tabla hecho_etiquetas
            Join<Hecho, String> etiquetasJoin = root.join("etiquetas", JoinType.INNER);

            // Creamos condición OR para que coincida con cualquiera de las etiquetas buscadas
            List<Predicate> etiquetaPredicates = new ArrayList<>();
            for (String etiqueta : es.getEtiquetas()) {
                etiquetaPredicates.add(
                    cb.like(cb.lower(etiquetasJoin), "%" + etiqueta.toLowerCase() + "%")
                );
            }

            return cb.or(etiquetaPredicates.toArray(new Predicate[0]));
        }

        // Si llegamos aquí, el strategy no es soportado por SQL
        // Se manejará en memoria como fallback
        return null;
    }

    public static boolean esSoportadoPorSQL(HechoStrategy strategy) {
        return strategy instanceof TituloStrategy ||
               strategy instanceof CategoriaStrategy ||
               strategy instanceof DescripcionStrategy ||
               strategy instanceof OrigenStrategy ||
               strategy instanceof FechaAcontecimientoStrategy ||
               strategy instanceof LongitudStrategy ||
               strategy instanceof LatitudStrategy ||
               strategy instanceof EstadoStrategy ||
               strategy instanceof FechaCargaStrategy ||
               strategy instanceof EtiquetasStrategy;
    }

    public static boolean todosSoportadosPorSQL(List<HechoStrategy> strategies) {
        return strategies.stream().allMatch(StrategyToSQLAdapter::esSoportadoPorSQL);
    }
}
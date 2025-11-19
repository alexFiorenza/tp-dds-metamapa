package utn.dds.agregador.controller;

import io.javalin.http.Context;
import utn.dds.dominio.criterios.*;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FiltroFactory {

    private static final Map<String, Function<String, HechoStrategy>> FILTROS = new HashMap<>();

    static {
        FILTROS.put("categoria", valor -> new CategoriaStrategy(valor));
        FILTROS.put("titulo", valor -> new TituloStrategy(valor));
        FILTROS.put("descripcion", valor -> new DescripcionStrategy(valor));
        FILTROS.put("origen", valor -> new OrigenStrategy(valor));

        FILTROS.put("fechaAcontecimiento", valor -> {
            try {
                LocalDate fecha = LocalDate.parse(valor);
                return new FechaAcontecimientoStrategy(fecha);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha de acontecimiento inválido. Use YYYY-MM-DD");
            }
        });

        FILTROS.put("longitud", valor -> {
            try {
                double lng = Double.parseDouble(valor);
                return new LongitudStrategy(lng);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato de longitud inválido");
            }
        });

        FILTROS.put("latitud", valor -> {
            try {
                double lat = Double.parseDouble(valor);
                return new LatitudStrategy(lat);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato de latitud inválido");
            }
        });

        FILTROS.put("estado", valor -> {
            try {
                EstadoHecho estado = EstadoHecho.valueOf(valor.toUpperCase());
                return new EstadoStrategy(estado);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Estado inválido. Use: ACTIVO, OCULTO");
            }
        });

        FILTROS.put("fechaCarga", valor -> {
            try {
                LocalDateTime fechaDateTime = LocalDateTime.parse(valor);
                return new FechaCargaStrategy(fechaDateTime);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Formato de fecha de carga inválido. Use YYYY-MM-DDTHH:mm:ss");
            }
        });

        FILTROS.put("etiquetas", valor -> {
            List<String> listaEtiquetas = Arrays.stream(valor.split(","))
                    .map(String::trim)
                    .filter(e -> !e.isEmpty())
                    .collect(Collectors.toList());

            if (listaEtiquetas.isEmpty()) {
                throw new IllegalArgumentException("Lista de etiquetas vacía");
            }

            return new EtiquetasStrategy(listaEtiquetas);
        });
    }

    public static List<HechoStrategy> crearFiltros(Context ctx) {
        List<HechoStrategy> filtros = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        for (Map.Entry<String, Function<String, HechoStrategy>> entry : FILTROS.entrySet()) {
            String parametro = entry.getKey();
            String valor = ctx.queryParam(parametro);

            if (valor != null && !valor.isEmpty()) {
                try {
                    HechoStrategy strategy = entry.getValue().apply(valor);
                    filtros.add(strategy);
                } catch (Exception e) {
                    errores.add("Error en parámetro '" + parametro + "': " + e.getMessage());
                }
            }
        }

        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errores));
        }

        return filtros;
    }

    /**
     * Crea filtros desde un Map (para GraphQL)
     */
    public static List<HechoStrategy> crearFiltrosDesdeMap(Map<String, Object> filtrosMap) {
        List<HechoStrategy> filtros = new ArrayList<>();
        List<String> errores = new ArrayList<>();

        if (filtrosMap == null || filtrosMap.isEmpty()) {
            return filtros;
        }

        for (Map.Entry<String, Function<String, HechoStrategy>> entry : FILTROS.entrySet()) {
            String parametro = entry.getKey();
            Object valor = filtrosMap.get(parametro);

            if (valor != null) {
                try {
                    String valorStr = valor.toString();
                    if (!valorStr.isEmpty()) {
                        HechoStrategy strategy = entry.getValue().apply(valorStr);
                        filtros.add(strategy);
                    }
                } catch (Exception e) {
                    errores.add("Error en parámetro '" + parametro + "': " + e.getMessage());
                }
            }
        }

        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(String.join("; ", errores));
        }

        return filtros;
    }

    public static Set<String> obtenerParametrosDisponibles() {
        return FILTROS.keySet();
    }
}

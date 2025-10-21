package utn.dds.dominio;

import jakarta.persistence.*;
import utn.dds.dominio.criterios.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Table(name = "criterios")
public class Criterio {

    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "id_coleccion")
    private String idColeccion;

    @Column(name = "tipo", nullable = false)
    private String tipo;

    // Campos para diferentes tipos de criterios
    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "categoria")
    private String categoria;

    @Column(name = "fecha_acontecimiento")
    private LocalDate fechaAcontecimiento;

    @Column(name = "origen")
    private String origen;

    @Column(name = "longitud")
    private Double longitud;

    @Column(name = "latitud")
    private Double latitud;

    @Column(name = "fecha_carga")
    private LocalDateTime fechaCarga;

    @Column(name = "etiquetas")
    private String etiquetas; // Comma-separated values

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "estado")
    private String estado;

    public Criterio() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Constructor para facilitar la creación
    public Criterio(String idColeccion, String tipo) {
        this();
        this.idColeccion = idColeccion;
        this.tipo = tipo;
    }

    // Método para convertir a HechoStrategy
    public HechoStrategy toHechoStrategy() {
        if ("titulo".equals(tipo)) {
            return new TituloStrategy(titulo);
        }
        else if ("categoria".equals(tipo)) {
            return new CategoriaStrategy(categoria);
        }
        else if ("descripcion".equals(tipo)) {
            return new DescripcionStrategy(descripcion);
        }
        else if ("origen".equals(tipo)) {
            return new OrigenStrategy(origen);
        }
        else if ("fechaAcontecimiento".equals(tipo)) {
            return new FechaAcontecimientoStrategy(fechaAcontecimiento);
        }
        else if ("longitud".equals(tipo)) {
            return new LongitudStrategy(longitud);
        }
        else if ("latitud".equals(tipo)) {
            return new LatitudStrategy(latitud);
        }
        else if ("estado".equals(tipo)) {
            EstadoHecho estadoEnum = EstadoHecho.valueOf(estado);
            return new EstadoStrategy(estadoEnum);
        }
        else if ("fechaCarga".equals(tipo)) {
            return new FechaCargaStrategy(fechaCarga);
        }
        else if ("etiquetas".equals(tipo)) {
            List<String> etiquetasList = Arrays.stream(etiquetas.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            return new EtiquetasStrategy(etiquetasList);
        }

        throw new IllegalArgumentException("Tipo de criterio no soportado: " + tipo);
    }

    // Método estático para crear desde HechoStrategy
    public static Criterio fromHechoStrategy(HechoStrategy strategy, String idColeccion) {
        if (strategy == null) {
            return null;
        }

        Criterio criterio = new Criterio(idColeccion, getTipoFromStrategy(strategy));

        if (strategy instanceof TituloStrategy) {
            TituloStrategy ts = (TituloStrategy) strategy;
            criterio.setTitulo(ts.getTitulo());
        }
        else if (strategy instanceof CategoriaStrategy) {
            CategoriaStrategy cs = (CategoriaStrategy) strategy;
            criterio.setCategoria(cs.getCategoria());
        }
        else if (strategy instanceof DescripcionStrategy) {
            DescripcionStrategy ds = (DescripcionStrategy) strategy;
            criterio.setDescripcion(ds.getDescripcion());
        }
        else if (strategy instanceof OrigenStrategy) {
            OrigenStrategy os = (OrigenStrategy) strategy;
            criterio.setOrigen(os.getOrigen());
        }
        else if (strategy instanceof FechaAcontecimientoStrategy) {
            FechaAcontecimientoStrategy fas = (FechaAcontecimientoStrategy) strategy;
            criterio.setFechaAcontecimiento(fas.getFechaAcontecimiento());
        }
        else if (strategy instanceof LongitudStrategy) {
            LongitudStrategy ls = (LongitudStrategy) strategy;
            criterio.setLongitud(ls.getLongitud());
        }
        else if (strategy instanceof LatitudStrategy) {
            LatitudStrategy ls = (LatitudStrategy) strategy;
            criterio.setLatitud(ls.getLatitud());
        }
        else if (strategy instanceof EstadoStrategy) {
            EstadoStrategy es = (EstadoStrategy) strategy;
            criterio.setEstado(es.getEstado().toString());
        }
        else if (strategy instanceof FechaCargaStrategy) {
            FechaCargaStrategy fcs = (FechaCargaStrategy) strategy;
            criterio.setFechaCarga(fcs.getFechaCarga());
        }
        else if (strategy instanceof EtiquetasStrategy) {
            EtiquetasStrategy es = (EtiquetasStrategy) strategy;
            String etiquetasStr = String.join(",", es.getEtiquetas());
            criterio.setEtiquetas(etiquetasStr);
        }

        return criterio;
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

    // Getters
    public String getUuid() {
        return uuid;
    }

    public String getIdColeccion() {
        return idColeccion;
    }

    public String getTipo() {
        return tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public LocalDate getFechaAcontecimiento() {
        return fechaAcontecimiento;
    }

    public String getOrigen() {
        return origen;
    }

    public Double getLongitud() {
        return longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public String getEtiquetas() {
        return etiquetas;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getEstado() {
        return estado;
    }

    // Setters
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setIdColeccion(String idColeccion) {
        this.idColeccion = idColeccion;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public void setFechaAcontecimiento(LocalDate fechaAcontecimiento) {
        this.fechaAcontecimiento = fechaAcontecimiento;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public void setEtiquetas(String etiquetas) {
        this.etiquetas = etiquetas;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}

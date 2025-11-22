package utn.dds.dto;

import utn.dds.dominio.criterios.*;
import utn.dds.dominio.EstadoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class CriterioCreateDTO {
    private String tipo;

    // Campos para diferentes tipos de criterios - solo se usa el campo correspondiente al tipo
    private String titulo;
    private String categoria;
    private String descripcion;
    private String origen;
    private LocalDate fechaAcontecimiento;
    private LocalDateTime fechaCarga;
    private Double longitud;
    private Double latitud;
    private String estado;
    private String etiquetas; // Comma-separated values para múltiples etiquetas

    public CriterioCreateDTO() {}

    // Método para convertir DTO a HechoStrategy
    public HechoStrategy toHechoStrategy() {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de criterio es obligatorio");
        }

        String tipoNormalizado = tipo.toLowerCase().trim();

        switch (tipoNormalizado) {
            case "titulo":
                if (titulo == null || titulo.trim().isEmpty()) {
                    throw new IllegalArgumentException("El campo 'titulo' es obligatorio para criterio tipo 'titulo'");
                }
                return new TituloStrategy(titulo);

            case "categoria":
                if (categoria == null || categoria.trim().isEmpty()) {
                    throw new IllegalArgumentException("El campo 'categoria' es obligatorio para criterio tipo 'categoria'");
                }
                return new CategoriaStrategy(categoria);

            case "descripcion":
                if (descripcion == null || descripcion.trim().isEmpty()) {
                    throw new IllegalArgumentException("El campo 'descripcion' es obligatorio para criterio tipo 'descripcion'");
                }
                return new DescripcionStrategy(descripcion);

            case "origen":
                if (origen == null || origen.trim().isEmpty()) {
                    throw new IllegalArgumentException("El campo 'origen' es obligatorio para criterio tipo 'origen'");
                }
                return new OrigenStrategy(origen);

            case "fechaacontecimiento":
            case "fecha_acontecimiento":
                if (fechaAcontecimiento == null) {
                    throw new IllegalArgumentException("El campo 'fechaAcontecimiento' es obligatorio para criterio tipo 'fechaAcontecimiento'");
                }
                return new FechaAcontecimientoStrategy(fechaAcontecimiento);

            case "fechacarga":
            case "fecha_carga":
                if (fechaCarga == null) {
                    throw new IllegalArgumentException("El campo 'fechaCarga' es obligatorio para criterio tipo 'fechaCarga'");
                }
                return new FechaCargaStrategy(fechaCarga);

            case "longitud":
                if (longitud == null) {
                    throw new IllegalArgumentException("El campo 'longitud' es obligatorio para criterio tipo 'longitud'");
                }
                return new LongitudStrategy(longitud);

            case "latitud":
                if (latitud == null) {
                    throw new IllegalArgumentException("El campo 'latitud' es obligatorio para criterio tipo 'latitud'");
                }
                return new LatitudStrategy(latitud);

            case "estado":
                if (estado == null || estado.trim().isEmpty()) {
                    throw new IllegalArgumentException("El campo 'estado' es obligatorio para criterio tipo 'estado'");
                }
                EstadoHecho estadoEnum = EstadoHecho.valueOf(estado.toUpperCase());
                return new EstadoStrategy(estadoEnum);

            case "etiquetas":
                if (etiquetas == null || etiquetas.trim().isEmpty()) {
                    throw new IllegalArgumentException("El campo 'etiquetas' es obligatorio para criterio tipo 'etiquetas'");
                }
                List<String> etiquetasList = Arrays.asList(etiquetas.split(","));
                return new EtiquetasStrategy(etiquetasList);

            default:
                throw new IllegalArgumentException("Tipo de criterio no válido: " + tipo + ". Tipos válidos: titulo, categoria, descripcion, origen, fechaAcontecimiento, fechaCarga, longitud, latitud, estado, etiquetas");
        }
    }

    // Getters y Setters
    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public LocalDate getFechaAcontecimiento() {
        return fechaAcontecimiento;
    }

    public void setFechaAcontecimiento(LocalDate fechaAcontecimiento) {
        this.fechaAcontecimiento = fechaAcontecimiento;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(String etiquetas) {
        this.etiquetas = etiquetas;
    }
}
package utn.dds.dto;

import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.TipoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HechoDTO {
    private String titulo;
    private String descripcion;
    private String categoria;
    private String fechaAcontecimiento; // ISO-8601: "2025-10-29"
    private String origen;
    private String contribuyenteNombre;
    private TipoHecho tipo;
    private double longitud;
    private double latitud;
    private String fechaCarga; // ISO-8601: "2025-10-29T22:24:15"
    private EstadoHecho estado;
    private List<String> etiquetas;
    private String uuid;

    public HechoDTO() {}

    public HechoDTO(String titulo, String descripcion, String categoria, String fechaAcontecimiento,
                    String origen, String contribuyenteNombre, TipoHecho tipo,
                    double longitud, double latitud, String fechaCarga,
                    EstadoHecho estado, List<String> etiquetas, String uuid) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.categoria = categoria;
        this.fechaAcontecimiento = fechaAcontecimiento;
        this.origen = origen;
        this.contribuyenteNombre = contribuyenteNombre;
        this.tipo = tipo;
        this.longitud = longitud;
        this.latitud = latitud;
        this.fechaCarga = fechaCarga;
        this.estado = estado;
        this.etiquetas = etiquetas;
        this.uuid = uuid;
    }

    public static HechoDTO fromHecho(Hecho hecho) {
        return new HechoDTO(
            hecho.getTitulo(),
            hecho.getDescripcion(),
            hecho.getCategoria(),
            hecho.getFechaAcontecimiento() != null ? hecho.getFechaAcontecimiento().format(DateTimeFormatter.ISO_LOCAL_DATE) : null,
            hecho.getOrigen(),
            hecho.getContribuyente() != null ? hecho.getContribuyente().getNombre() : null,
            hecho.getTipo(),
            hecho.getLongitud(),
            hecho.getLatitud(),
            hecho.getFechaCarga() != null ? hecho.getFechaCarga().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
            hecho.getEstado(),
            hecho.getEtiquetas(),
            hecho.getUuid()
        );
    }

    public Hecho toHecho() {
        return new Hecho(
            this.titulo,
            this.descripcion,
            this.categoria,
            this.fechaAcontecimiento != null ? LocalDate.parse(this.fechaAcontecimiento, DateTimeFormatter.ISO_LOCAL_DATE) : null,
            this.origen,
            null, // contribuyente - en el mock es null
            this.tipo,
            this.longitud,
            this.latitud,
            this.fechaCarga != null ? LocalDateTime.parse(this.fechaCarga, DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null,
            this.estado,
            this.etiquetas
        );
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getFechaAcontecimiento() {
        return fechaAcontecimiento;
    }

    public void setFechaAcontecimiento(String fechaAcontecimiento) {
        this.fechaAcontecimiento = fechaAcontecimiento;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public String getContribuyenteNombre() {
        return contribuyenteNombre;
    }

    public void setContribuyenteNombre(String contribuyenteNombre) {
        this.contribuyenteNombre = contribuyenteNombre;
    }

    public TipoHecho getTipo() {
        return tipo;
    }

    public void setTipo(TipoHecho tipo) {
        this.tipo = tipo;
    }

    public double getLongitud() {
        return longitud;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public String getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(String fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public EstadoHecho getEstado() {
        return estado;
    }

    public void setEstado(EstadoHecho estado) {
        this.estado = estado;
    }

    public List<String> getEtiquetas() {
        return etiquetas;
    }

    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
}
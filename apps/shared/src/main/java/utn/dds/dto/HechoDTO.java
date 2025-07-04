package utn.dds.dto;

import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.Origen;
import utn.dds.dominio.TipoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class HechoDTO {
    private String titulo;
    private String descripcion;
    private String categoria;
    private LocalDate fechaAcontecimiento;
    private Origen origen;
    private String contribuyenteNombre;
    private TipoHecho tipo;
    private double longitud;
    private double latitud;
    private LocalDateTime fechaCarga;
    private EstadoHecho estado;
    private List<String> etiquetas;
    private String uuid;

    public HechoDTO() {}

    public HechoDTO(String titulo, String descripcion, String categoria, LocalDate fechaAcontecimiento,
                    Origen origen, String contribuyenteNombre, TipoHecho tipo,
                    double longitud, double latitud, LocalDateTime fechaCarga,
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
            hecho.getFechaAcontecimiento(),
            hecho.getOrigen(),
            hecho.getContribuyente() != null ? hecho.getContribuyente().getNombre() : null,
            hecho.getTipo(),
            hecho.getLongitud(),
            hecho.getLatitud(),
            hecho.getFechaCarga(),
            hecho.getEstado(),
            hecho.getEtiquetas(),
            hecho.getUuid()
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

    public LocalDate getFechaAcontecimiento() {
        return fechaAcontecimiento;
    }

    public void setFechaAcontecimiento(LocalDate fechaAcontecimiento) {
        this.fechaAcontecimiento = fechaAcontecimiento;
    }

    public Origen getOrigen() {
        return origen;
    }

    public void setOrigen(Origen origen) {
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

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
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
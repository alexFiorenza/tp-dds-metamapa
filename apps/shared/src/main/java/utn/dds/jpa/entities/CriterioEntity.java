package utn.dds.jpa.entities;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "criterios")
public class CriterioEntity {

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

    public CriterioEntity() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Constructor para facilitar la creación
    public CriterioEntity(String idColeccion, String tipo) {
        this();
        this.idColeccion = idColeccion;
        this.tipo = tipo;
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
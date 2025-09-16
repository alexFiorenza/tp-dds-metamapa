package utn.dds.jpa.entities;

import jakarta.persistence.*;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Origen;
import utn.dds.dominio.TipoHecho;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "hechos")
public class HechoEntity {
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "titulo", nullable = false, length = 500)
    private String titulo;

    @Column(name = "descripcion", length = 2000)
    private String descripcion;

    @Column(name = "categoria", length = 500)
    private String categoria;

    @Column(name = "fecha_acontecimiento")
    private LocalDate fechaAcontecimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "origen")
    private Origen origen;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "contribuyente_id")
    private ContribuyenteEntity contribuyente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo")
    private TipoHecho tipo;

    @Column(name = "longitud")
    private double longitud;

    @Column(name = "latitud")
    private double latitud;

    @Column(name = "fecha_carga")
    private LocalDateTime fechaCarga;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoHecho estado;

    @ElementCollection
    @CollectionTable(name = "hecho_etiquetas", joinColumns = @JoinColumn(name = "hecho_uuid"))
    @Column(name = "etiqueta")
    private List<String> etiquetas;

    public HechoEntity() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Getters
    public String getUuid() {
        return uuid;
    }

    public String getTitulo() {
        return titulo;
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

    public Origen getOrigen() {
        return origen;
    }

    public ContribuyenteEntity getContribuyente() {
        return contribuyente;
    }

    public TipoHecho getTipo() {
        return tipo;
    }

    public double getLongitud() {
        return longitud;
    }

    public double getLatitud() {
        return latitud;
    }

    public LocalDateTime getFechaCarga() {
        return fechaCarga;
    }

    public EstadoHecho getEstado() {
        return estado;
    }

    public List<String> getEtiquetas() {
        return etiquetas;
    }

    // Setters
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
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

    public void setOrigen(Origen origen) {
        this.origen = origen;
    }

    public void setContribuyente(ContribuyenteEntity contribuyente) {
        this.contribuyente = contribuyente;
    }

    public void setTipo(TipoHecho tipo) {
        this.tipo = tipo;
    }

    public void setLongitud(double longitud) {
        this.longitud = longitud;
    }

    public void setLatitud(double latitud) {
        this.latitud = latitud;
    }

    public void setFechaCarga(LocalDateTime fechaCarga) {
        this.fechaCarga = fechaCarga;
    }

    public void setEstado(EstadoHecho estado) {
        this.estado = estado;
    }

    public void setEtiquetas(List<String> etiquetas) {
        this.etiquetas = etiquetas;
    }
}
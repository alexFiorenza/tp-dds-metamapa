package utn.dds.dto;

import utn.dds.dominio.Coleccion;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.criterios.HechoStrategy;

import java.util.List;
import java.util.stream.Collectors;

public class ColeccionDTO {
    private String titulo;
    private String descripcion;
    private List<HechoDTO> hechos;
    private String handle;
    private int cantidadHechos;

    // Constructor vacío para deserialización
    public ColeccionDTO() {}

    // Constructor completo
    public ColeccionDTO(String titulo, String descripcion, List<HechoDTO> hechos, String handle, int cantidadHechos) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.hechos = hechos;
        this.handle = handle;
        this.cantidadHechos = cantidadHechos;
    }

    // Método estático para crear DTO desde entidad de dominio
    public static ColeccionDTO fromColeccion(Coleccion coleccion) {
        List<HechoDTO> hechosDTO = coleccion.getHechos().stream()
            .map(HechoDTO::fromHecho)
            .collect(Collectors.toList());
        
        return new ColeccionDTO(
            coleccion.getTitulo(),
            coleccion.getHechos().isEmpty() ? "" : "Colección de " + coleccion.getHechos().size() + " hechos",
            hechosDTO,
            coleccion.getTitulo(), // Usando título como handle por simplicidad
            coleccion.getHechos().size()
        );
    }

    // Método para crear DTO básico sin hechos (para listados)
    public static ColeccionDTO fromColeccionBasic(Coleccion coleccion) {
        return new ColeccionDTO(
            coleccion.getTitulo(),
            coleccion.getHechos().isEmpty() ? "" : "Colección de " + coleccion.getHechos().size() + " hechos",
            null, // No incluir hechos en listados
            coleccion.getTitulo(),
            coleccion.getHechos().size()
        );
    }

    // Getters y Setters
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

    public List<HechoDTO> getHechos() {
        return hechos;
    }

    public void setHechos(List<HechoDTO> hechos) {
        this.hechos = hechos;
    }

    public String getHandle() {
        return handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    public int getCantidadHechos() {
        return cantidadHechos;
    }

    public void setCantidadHechos(int cantidadHechos) {
        this.cantidadHechos = cantidadHechos;
    }
}
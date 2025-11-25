package utn.dds.dto;

import utn.dds.dominio.Coleccion;
import utn.dds.dominio.consenso.AlgoritmoConsensoFactory;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ColeccionDTO {
    private String handle;
    private String titulo;
    private String descripcion;
    private List<FuenteDTO> fuentes;
    private List<CriterioCreateDTO> criteriosDePertenencia;
    private String algoritmoConsenso; // String para la API REST

    public ColeccionDTO() {}

    public ColeccionDTO(String handle, String titulo, String descripcion,
                       List<FuenteDTO> fuentes,
                       List<CriterioCreateDTO> criteriosDePertenencia,
                       String algoritmoConsenso) {
        this.handle = handle;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fuentes = fuentes;
        this.criteriosDePertenencia = criteriosDePertenencia;
        this.algoritmoConsenso = algoritmoConsenso;
    }

    // Factory method para convertir desde dominio
    public static ColeccionDTO from(Coleccion coleccion) {
        if (coleccion == null) {
            return null;
        }

        ColeccionDTO dto = new ColeccionDTO();
        dto.setHandle(coleccion.getHandle());
        dto.setTitulo(coleccion.getTitulo());
        dto.setDescripcion(coleccion.getDescripcion());

        // Mapear fuentes
        if (coleccion.getFuentes() != null) {
            dto.setFuentes(
                coleccion.getFuentes().stream()
                    .map(FuenteDTO::from)
                    .collect(Collectors.toList())
            );
        } else {
            dto.setFuentes(new ArrayList<>());
        }

        // Mapear criterios
        if (coleccion.getCriteriosDePertenencia() != null) {
            dto.setCriteriosDePertenencia(
                coleccion.getCriteriosDePertenencia().stream()
                    .map(criterio -> {
                        CriterioCreateDTO criterioDTO = new CriterioCreateDTO();
                        criterioDTO.setTipo(criterio.getTipo());
                        // Mapear todos los campos del criterio
                        criterioDTO.setTitulo(criterio.getTitulo());
                        criterioDTO.setCategoria(criterio.getCategoria());
                        criterioDTO.setDescripcion(criterio.getDescripcion());
                        criterioDTO.setOrigen(criterio.getOrigen());
                        criterioDTO.setFechaAcontecimiento(criterio.getFechaAcontecimiento());
                        criterioDTO.setFechaCarga(criterio.getFechaCarga());
                        criterioDTO.setLongitud(criterio.getLongitud());
                        criterioDTO.setLatitud(criterio.getLatitud());
                        criterioDTO.setEstado(criterio.getEstado());
                        criterioDTO.setEtiquetas(criterio.getEtiquetas());
                        return criterioDTO;
                    })
                    .collect(Collectors.toList())
            );
        } else {
            dto.setCriteriosDePertenencia(new ArrayList<>());
        }

        // Mapear algoritmo de consenso (convertir de interfaz a String)
        dto.setAlgoritmoConsenso(coleccion.getAlgoritmoConsenso() != null ? 
            AlgoritmoConsensoFactory.obtenerTipo(coleccion.getAlgoritmoConsenso()) : "default");

        return dto;
    }

    // Getters
    public String getHandle() {
        return handle;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public List<FuenteDTO> getFuentes() {
        return fuentes;
    }

    public List<CriterioCreateDTO> getCriteriosDePertenencia() {
        return criteriosDePertenencia;
    }

    // Setters
    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFuentes(List<FuenteDTO> fuentes) {
        this.fuentes = fuentes;
    }

    public void setCriteriosDePertenencia(List<CriterioCreateDTO> criteriosDePertenencia) {
        this.criteriosDePertenencia = criteriosDePertenencia;
    }

    public String getAlgoritmoConsenso() {
        return algoritmoConsenso;
    }

    public void setAlgoritmoConsenso(String algoritmoConsenso) {
        this.algoritmoConsenso = algoritmoConsenso;
    }
}
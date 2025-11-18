package utn.dds.dto;

import utn.dds.dominio.Fuente;
import java.util.Map;
import java.util.UUID;

public class FuenteDTO {
    private String host;
    private Map<String, Object> params;
    private UUID uuid;
    private String tipo;
    private Map<String, Object> metadata;

    public FuenteDTO() {}

    public FuenteDTO(String host, Map<String, Object> params, UUID uuid) {
        this.host = host;
        this.params = params;
        this.uuid = uuid;
    }

    public FuenteDTO(String host, Map<String, Object> params, UUID uuid, String tipo, Map<String, Object> metadata) {
        this.host = host;
        this.params = params;
        this.uuid = uuid;
        this.tipo = tipo;
        this.metadata = metadata;
    }

    // Factory method para convertir desde dominio
    public static FuenteDTO from(Fuente fuente) {
        if (fuente == null) {
            return null;
        }
        return new FuenteDTO(
            fuente.getHost(),
            fuente.getParams(),
            UUID.fromString(fuente.getUuid()),
            fuente.getTipo(),
            fuente.getMetadata()
        );
    }

    // Método para convertir a dominio
    public Fuente toFuente() {
        Fuente fuente = new Fuente();
        if (this.uuid != null) {
            fuente.setUuid(this.uuid.toString());
        }
        fuente.setHost(this.host);
        fuente.setParams(this.params);
        fuente.setTipo(this.tipo);
        fuente.setMetadata(this.metadata);
        return fuente;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
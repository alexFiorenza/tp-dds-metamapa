package utn.dds.dto;

import utn.dds.dominio.Fuente;
import java.util.Map;
import java.util.UUID;

public class FuenteDTO {
    private String host;
    private Map<String, Object> params;
    private UUID uuid;

    public FuenteDTO() {}

    public FuenteDTO(String host, Map<String, Object> params, UUID uuid) {
        this.host = host;
        this.params = params;
        this.uuid = uuid;
    }

    // Factory method para convertir desde dominio
    public static FuenteDTO from(Fuente fuente) {
        if (fuente == null) {
            return null;
        }
        return new FuenteDTO(
            fuente.getHost(),
            fuente.getParams(),
            UUID.fromString(fuente.getUuid())
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
}
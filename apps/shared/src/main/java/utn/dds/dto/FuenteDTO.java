package utn.dds.dto;

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
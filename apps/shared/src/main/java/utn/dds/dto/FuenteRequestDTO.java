package utn.dds.dto;

import java.util.Map;

/**
 * DTO para requests de registro de fuentes (sin UUID)
 */
public class FuenteRequestDTO {
    private String host;
    private Map<String, Object> params;
    private String tipo;
    private Map<String, Object> metadata;

    public FuenteRequestDTO() {}

    public FuenteRequestDTO(String host, Map<String, Object> params) {
        this.host = host;
        this.params = params;
    }

    public FuenteRequestDTO(String host, Map<String, Object> params, String tipo, Map<String, Object> metadata) {
        this.host = host;
        this.params = params;
        this.tipo = tipo;
        this.metadata = metadata;
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
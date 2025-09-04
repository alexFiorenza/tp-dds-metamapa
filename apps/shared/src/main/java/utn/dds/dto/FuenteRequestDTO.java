package utn.dds.dto;

import java.util.Map;

/**
 * DTO para requests de registro de fuentes (sin UUID)
 */
public class FuenteRequestDTO {
    private String host;
    private Map<String, Object> params;

    public FuenteRequestDTO() {}

    public FuenteRequestDTO(String host, Map<String, Object> params) {
        this.host = host;
        this.params = params;
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
}
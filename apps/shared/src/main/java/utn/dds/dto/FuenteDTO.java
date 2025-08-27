package utn.dds.dto;

import java.util.UUID;

public class FuenteDTO {
    private String url;
    private UUID uuid;

    public FuenteDTO() {}

    public FuenteDTO(String url, UUID uuid) {
        this.url = url;
        this.uuid = uuid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
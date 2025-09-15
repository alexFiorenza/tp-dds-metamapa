package utn.dds.jpa.entities;

import jakarta.persistence.*;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "fuentes")
public class FuenteEntity {
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "host", nullable = false)
    private String host;

    @ElementCollection
    @CollectionTable(name = "fuente_params", joinColumns = @JoinColumn(name = "fuente_uuid"))
    @MapKeyColumn(name = "param_key")
    @Column(name = "param_value")
    private Map<String, String> params;

    public FuenteEntity() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Getters
    public String getUuid() {
        return uuid;
    }

    public String getHost() {
        return host;
    }

    public Map<String, String> getParams() {
        return params;
    }

    // Setters
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }
}
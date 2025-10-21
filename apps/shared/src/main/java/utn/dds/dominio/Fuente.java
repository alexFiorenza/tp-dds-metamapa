package utn.dds.dominio;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "fuentes")
public class Fuente {
    @Id
    @Column(name = "uuid")
    private String uuid;

    @Column(name = "host", nullable = false)
    private String host;

    @Column(name = "params", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> params;

    public Fuente() {
        this.uuid = UUID.randomUUID().toString();
    }

    // Getters
    public String getUuid() {
        return uuid;
    }

    public String getHost() {
        return host;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    // Setters
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}

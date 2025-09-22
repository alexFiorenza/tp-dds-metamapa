package utn.dds.dominio;

public enum Origen {
    DATASET,
    MANUAL,
    CONTRIBUYENTE;

    private String fuenteId;

    public static Origen fromFuenteId(String fuenteId) {
        Origen origen = DATASET;
        origen.fuenteId = fuenteId;
        return origen;
    }

    public String getFuenteId() {
        return fuenteId;
    }

    public void setFuenteId(String fuenteId) {
        this.fuenteId = fuenteId;
    }
} 
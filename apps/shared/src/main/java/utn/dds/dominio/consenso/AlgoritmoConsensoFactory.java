package utn.dds.dominio.consenso;

/**
 * Factory para crear instancias de algoritmos de consenso basándose en el tipo almacenado.
 */
public class AlgoritmoConsensoFactory {
    
    /**
     * Crea una instancia del algoritmo de consenso basándose en el tipo.
     * 
     * @param tipo Tipo del algoritmo: "menciones", "simple", "absoluta", "default" o null
     * @return Instancia del algoritmo de consenso correspondiente
     */
    public static AlgoritmoConsenso crear(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return new ConsensoDefault();
        }

        switch (tipo.toUpperCase()) {
            case "MENCIONES":
                return new ConsensoMenciones();
            case "SIMPLE":
                return new ConsensoSimple();
            case "ABSOLUTA":
                return new ConsensoAbsoluto();
            case "DEFAULT":
            default:
                return new ConsensoDefault();
        }
    }

    /**
     * Obtiene el tipo (String) de un algoritmo de consenso para persistirlo.
     * 
     * @param algoritmo Instancia del algoritmo de consenso
     * @return String que representa el tipo del algoritmo
     */
    public static String obtenerTipo(AlgoritmoConsenso algoritmo) {
        if (algoritmo == null) {
            return "default";
        }

        if (algoritmo instanceof ConsensoMenciones) {
            return "menciones";
        } else if (algoritmo instanceof ConsensoSimple) {
            return "simple";
        } else if (algoritmo instanceof ConsensoAbsoluto) {
            return "absoluta";
        } else {
            return "default";
        }
    }
}


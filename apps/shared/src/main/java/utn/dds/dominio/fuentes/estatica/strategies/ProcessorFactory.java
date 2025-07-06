package utn.dds.dominio.fuentes.estatica.strategies;

public class ProcessorFactory {
    
    public static ProcesadorStrategy createProcessor(String type) {
        switch (type.toLowerCase()) {
            case "csv":
                return new CSVStrategy();
            default:
                throw new IllegalArgumentException("Tipo de procesador no soportado: " + type);
        }
    }
}
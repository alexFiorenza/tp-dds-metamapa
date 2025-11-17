package utn.dds.dominio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class AlgoritmoTFIDF implements DetectorSpam {
    private static final Logger logger = LoggerFactory.getLogger(AlgoritmoTFIDF.class);

    // Palabras spam configurables desde variable de entorno
    private final List<String> palabrasSpam;

    // Patrones sospechosos
    private static final Pattern PATRON_URL_EXCESIVO = Pattern.compile("(https?://[^\\s]+.*){3,}");
    private static final Pattern PATRON_REPETICION = Pattern.compile("(.)\\1{4,}"); // Caracteres repetidos 5+ veces
    private static final Pattern PATRON_MAYUSCULAS = Pattern.compile("[A-Z]{10,}"); // 10+ mayúsculas seguidas

    public AlgoritmoTFIDF() {
        // Leer palabras spam desde variable de entorno SPAM_PALABRAS
        String palabrasEnv = System.getenv("SPAM_PALABRAS");

        if (palabrasEnv != null && !palabrasEnv.trim().isEmpty()) {
            this.palabrasSpam = Arrays.stream(palabrasEnv.split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());

            logger.info("Detector de spam inicializado con {} palabras desde variable de entorno",
                       this.palabrasSpam.size());
        } else {
            this.palabrasSpam = new ArrayList<>();
            logger.info("Detector de spam inicializado sin palabras spam configuradas");
        }
    }

    @Override
    public boolean esSpam(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return true; // Texto vacío es considerado spam
        }

        String textoLower = texto.toLowerCase();

        // 1. Verificar si el texto es muy corto (menos de 10 caracteres)
        if (texto.trim().length() < 10) {
            return true;
        }

        // 2. Verificar palabras spam (solo si hay palabras configuradas)
        for (String palabraSpam : palabrasSpam) {
            if (textoLower.contains(palabraSpam)) {
                logger.debug("Texto detectado como spam por palabra: {}", palabraSpam);
                return true;
            }
        }

        // 3. Verificar URLs excesivas (más de 2 URLs)
        if (PATRON_URL_EXCESIVO.matcher(texto).find()) {
            return true;
        }

        // 4. Verificar caracteres repetidos excesivamente
        if (PATRON_REPETICION.matcher(texto).find()) {
            return true;
        }

        // 5. Verificar uso excesivo de mayúsculas
        if (PATRON_MAYUSCULAS.matcher(texto).find()) {
            return true;
        }

        // 6. Verificar caracteres especiales excesivos
        long caracteresEspeciales = texto.chars()
            .filter(c -> !Character.isLetterOrDigit(c) && !Character.isWhitespace(c))
            .count();
        double ratioEspeciales = (double) caracteresEspeciales / texto.length();
        if (ratioEspeciales > 0.3) { // Más del 30% de caracteres especiales
            return true;
        }

        return false;
    }
} 
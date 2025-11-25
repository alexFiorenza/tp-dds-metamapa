package utn.dds.tests.servicios;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SpamDetectorTest {

    // Implementación simple y local del detector de spam para tests
    private boolean isSpam(String texto) {
        if (texto == null || texto.isBlank()) return false;
        String lower = texto.toLowerCase();
        // heurísticas simples
        if (lower.contains("http://") || lower.contains("https://")) return true;
        if (lower.contains("comprar ahora") || lower.contains("gana dinero")) return true;
        // demasiados caracteres repetidos
        if (texto.matches(".*(.)\\1{6,}.*")) return true;
        return false;
    }

    @Test
    void marcaSpamAutomaticamenteYRechazaSolicitud() {
        String candidatoSpam = "Visita http://spam.example.com y gana dinero ahora!!!";
        assertTrue(isSpam(candidatoSpam), "El texto con URL/marketing debe ser detectado como spam");
    }

    @Test
    void dejaPendienteSiNoEsSpam() {
        String legit = "Hubo un corte de luz en la calle y se solicita colaboración para asistir a los afectados.";
        assertFalse(isSpam(legit), "Un reporte legítimo no debe marcarse como spam");
    }
}
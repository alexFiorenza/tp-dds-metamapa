package utn.dds.agregador.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import utn.dds.dto.normalizador.ConfiguracionNormalizacionDTO;
import utn.dds.dto.normalizador.NormalizarRequestDTO;
import utn.dds.dto.normalizador.NormalizarResponseDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Cliente HTTP para comunicarse con el servicio de normalización.
 */
public class NormalizadorClient {

    private final String normalizadorUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final boolean habilitado;

    /**
     * Constructor del cliente del normalizador.
     * @param normalizadorUrl URL del servicio de normalización (ej: http://normalizador:3000)
     * @param habilitado Flag para habilitar/deshabilitar normalización
     */
    public NormalizadorClient(String normalizadorUrl, boolean habilitado) {
        this.normalizadorUrl = normalizadorUrl;
        this.habilitado = habilitado;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
        this.objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Normaliza una lista de hechos llamando al servicio de normalización.
     * Si la normalización está deshabilitada o falla, retorna los hechos sin cambios.
     *
     * @param hechos Lista de hechos a normalizar
     * @return Lista de hechos normalizados
     */
    public List<Hecho> normalizar(List<Hecho> hechos) {
        if (!habilitado) {
            System.out.println("[Normalizador] Normalización deshabilitada, retornando hechos sin cambios");
            return hechos;
        }

        if (hechos == null || hechos.isEmpty()) {
            return hechos;
        }

        try {
            System.out.println("[Normalizador] Normalizando " + hechos.size() + " hechos...");

            // Convertir Hechos a DTOs
            List<HechoDTO> hechosDTO = new ArrayList<>();
            for (Hecho hecho : hechos) {
                hechosDTO.add(HechoDTO.fromHecho(hecho));
            }

            // Crear request con configuración por defecto
            NormalizarRequestDTO request = new NormalizarRequestDTO(
                hechosDTO,
                ConfiguracionNormalizacionDTO.porDefecto()
            );

            // Serializar request a JSON
            String requestBody = objectMapper.writeValueAsString(request);

            // Crear request HTTP
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(normalizadorUrl + "/normalizar"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            // Enviar request
            HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (httpResponse.statusCode() != 200) {
                System.err.println("[Normalizador] Error HTTP " + httpResponse.statusCode() + ", retornando hechos sin normalizar");
                return hechos;
            }

            // Parsear respuesta
            NormalizarResponseDTO response = objectMapper.readValue(
                httpResponse.body(),
                NormalizarResponseDTO.class
            );

            // Log estadísticas
            if (response.getEstadisticas() != null) {
                System.out.println("[Normalizador] Procesados: " + response.getEstadisticas().getTotalProcesados() +
                        ", Normalizados: " + response.getEstadisticas().getTotalNormalizados() +
                        ", Rechazados: " + response.getEstadisticas().getTotalRechazados() +
                        ", Tiempo: " + response.getEstadisticas().getTiempoEjecucion() + "ms");
            }

            // Log hechos rechazados
            if (response.getHechosRechazados() != null && !response.getHechosRechazados().isEmpty()) {
                System.out.println("[Normalizador] Hechos rechazados:");
                response.getHechosRechazados().forEach(rechazado ->
                    System.out.println("  - UUID: " + rechazado.getHechoOriginal().getUuid() +
                            ", Motivo: " + rechazado.getMotivo())
                );
            }

            // Convertir DTOs normalizados de vuelta a Hechos
            List<Hecho> hechosNormalizados = new ArrayList<>();
            if (response.getHechosNormalizados() != null) {
                for (HechoDTO dto : response.getHechosNormalizados()) {
                    hechosNormalizados.add(dto.toHecho());
                }
            }

            return hechosNormalizados;

        } catch (Exception e) {
            System.err.println("[Normalizador] Error al normalizar hechos: " + e.getMessage());
            e.printStackTrace();
            // En caso de error, retornar hechos sin normalizar
            return hechos;
        }
    }

    /**
     * Verifica si el servicio de normalización está disponible.
     * @return true si el servicio responde correctamente
     */
    public boolean verificarDisponibilidad() {
        if (!habilitado) {
            return false;
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizadorUrl + "/health"))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;

        } catch (Exception e) {
            System.err.println("[Normalizador] Servicio no disponible: " + e.getMessage());
            return false;
        }
    }
}

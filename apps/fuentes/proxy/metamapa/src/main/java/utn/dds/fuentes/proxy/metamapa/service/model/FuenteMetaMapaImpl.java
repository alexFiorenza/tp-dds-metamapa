package utn.dds.fuentes.proxy.metamapa.service.model;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;

import java.util.List;
import java.util.ArrayList;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

public class FuenteMetaMapaImpl implements FuenteDeDatos {
    private final TipoFuente tipoFuente;
    private String url;
    private String handle;
        
    public FuenteMetaMapaImpl(String url) {
        this.tipoFuente = TipoFuente.PROXY;
        this.handle = null;
        this.url = url;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<>();
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(this.url + "/hechos"))
                .header("Accept", "application/json")
                .GET()
                .build();
            
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String responseBody = response.body();
                ObjectMapper mapper = new ObjectMapper();
                
                List<java.util.Map<String, Object>> data = mapper.readValue(responseBody, 
                    new TypeReference<List<java.util.Map<String, Object>>>() {});
                
                for (java.util.Map<String, Object> item : data) {
                    hechos.add(new Hecho(item));
                }
            } else {
                throw new RuntimeException("HTTP Request falló con estado: " + response.statusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al obtener hechos de MetaMapa: " + e.getMessage(), e);
        }
        
        return hechos;
    }

    @Override
    public TipoFuente tipo() {
        return this.tipoFuente;
    }
} 
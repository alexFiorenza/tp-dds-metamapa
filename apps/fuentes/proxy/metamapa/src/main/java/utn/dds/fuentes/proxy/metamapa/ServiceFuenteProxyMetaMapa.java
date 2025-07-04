package utn.dds.fuentes.proxy.metamapa;

import java.util.List;
import java.util.ArrayList;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import utn.dds.dominio.Hecho;

public class ServiceFuenteProxyMetaMapa {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFuenteProxyMetaMapa.class);
    private String url;

    public ServiceFuenteProxyMetaMapa(String url) {
        this.url = url;
    }
    
    public List<Hecho> obtenerHechos(){
        List<Hecho> hechos = new ArrayList<>();
        try {
            URL endpoint = new URL(this.url + "/hechos");
            HttpURLConnection conn = (HttpURLConnection) endpoint.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            int responseCode = conn.getResponseCode();
            logger.info("HTTP GET request to {} returned status: {}", endpoint, responseCode);
            
            if (responseCode == 200) {
                InputStream is = conn.getInputStream();
                ObjectMapper mapper = new ObjectMapper();
                
                List<java.util.Map<String, Object>> data = mapper.readValue(is, 
                    new TypeReference<List<java.util.Map<String, Object>>>() {});
                
                for (java.util.Map<String, Object> item : data) {
                    hechos.add(new Hecho(item));
                }
                
                is.close();
                logger.info("Successfully parsed {} hechos from response", hechos.size());
            } else {
                logger.error("HTTP request failed with status code: {}", responseCode);
            }
            
            conn.disconnect();
        } catch (Exception e) {
            logger.error("Error fetching hechos from {}: {}", this.url, e.getMessage(), e);
        }
        
        return hechos;
    }
}
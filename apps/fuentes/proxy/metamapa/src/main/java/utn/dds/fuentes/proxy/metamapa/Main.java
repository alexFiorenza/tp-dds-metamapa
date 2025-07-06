package utn.dds.fuentes.proxy.metamapa;

import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        // Read URL from environment variable
        String url = System.getenv("url");

        if (url == null) {
            url = "https://56d05c91-ca57-4d58-acd5-6d61368e622a.mock.pstmn.io"; // Usamos el mockserver de postman sino lo pasan
        }
        
        logger.info("Iniciando servicio con configuración:");
        logger.info("Metamapa URL: {}", url);

        // Crear servicio con instancias pre-configuradas
        ServiceFuenteProxyMetaMapa proxyMetamapaService = new ServiceFuenteProxyMetaMapa(url);
        ControllerProxyMetamapa controller = new ControllerProxyMetamapa(proxyMetamapaService);
        
        Javalin app = Javalin.create(config -> {
            config.plugins.enableDevLogging();
        }).start(7003);
        
        // Health check
        app.get("/health", ctx -> ctx.result("OK"));
        
        // Endpoint principal
        app.get("/", ctx -> {
            ctx.result("Proxy MetaMapa - MetaMapa");
        });
        
        // Endpoint para obtener datos del proxy
        app.get("/hechos", controller::obtenerHechos);
        
        logger.info("Servicio proxy MetaMapa iniciado en puerto 7003");
    }
} 
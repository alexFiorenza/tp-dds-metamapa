package utn.dds.fuentes.proxy.demo.service;
import java.util.List;
import java.util.ArrayList;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.MalformedURLException;
import java.net.URI;
import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dto.RespuestaPaginadaDTO;
import utn.dds.fuentes.proxy.demo.service.model.conexion.Conexion;
import utn.dds.fuentes.proxy.demo.service.model.FuenteDemoImpl;
import utn.dds.fuentes.proxy.demo.persistencia.HechoRepository;
import utn.dds.fuentes.proxy.demo.persistencia.EjecucionRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceFuenteProxyDemo {
    private static final Logger logger = LoggerFactory.getLogger(ServiceFuenteProxyDemo.class);
    private EjecucionRepository ejecucionRepository;
    private HechoRepository hechoRepository;
    private URL url;
    private FuenteDeDatos fuente;

    public ServiceFuenteProxyDemo(Conexion conexion, String daoType, Map<String, Object> daoConfig) throws MalformedURLException {
        try {
            this.url= URI.create("http://example.com/api/hechos").toURL();
        } catch (MalformedURLException | IllegalArgumentException e) {
            throw new RuntimeException("Error al crear URL", e);
        }

        this.ejecucionRepository = new EjecucionRepository(daoType, daoConfig);
        this.hechoRepository = new HechoRepository(daoType, daoConfig);
        this.fuente = new FuenteDemoImpl(conexion, this.url, ejecucionRepository.obtenerUltimaEjecucion());
    }

    public List<Hecho> obtenerHechos() {
        // Obtengo directamente de la cache y no uso la conexión
        return this.hechoRepository.obtener();
    }

    public void agregarHechos(){
        LocalDateTime ahora = LocalDateTime.now();
        String timestamp = ahora.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        logger.info("Iniciando actualización de fuente proxy demo el {}", timestamp);
        
        // Obtengo nuevos hechos desde la fuente
        List<Hecho> nuevosHechos= this.fuente.obtenerHechos();
        if(nuevosHechos != null){
            this.hechoRepository.actualizar(nuevosHechos);
            logger.info("✓ Fuente proxy demo actualizada correctamente el {} - {} hechos procesados", timestamp, nuevosHechos.size());
        } else {
            logger.info("✓ Cronjob de proxy demo ejecutado correctamente el {} - No hay nuevos hechos", timestamp);
        }
        
        this.ejecucionRepository.guardarUltimaEjecucion(ahora);
    }
    
    public RespuestaPaginadaDTO<Hecho> obtenerHechosPaginados(int pagina, int tamanioPagina) {
        logger.info("Obteniendo hechos paginados del proxy demo - página: {}, tamaño: {}", pagina, tamanioPagina);
        
        // Validaciones
        if (pagina < 0) {
            pagina = 0;
        }
        if (tamanioPagina <= 0) {
            tamanioPagina = 10; // Tamaño por defecto
        }
        if (tamanioPagina > 100) {
            tamanioPagina = 100; // Máximo 100 elementos por página
        }
        
        try {
            // Obtener todos los hechos del repositorio
            List<Hecho> todosLosHechos = this.hechoRepository.obtener();
            long totalElementos = todosLosHechos.size();
            
            // Calcular índices para la paginación
            int indiceInicio = pagina * tamanioPagina;
            int indiceFin = Math.min(indiceInicio + tamanioPagina, (int) totalElementos);
            
            // Obtener datos de la página actual
            List<Hecho> datosPagina;
            if (indiceInicio >= totalElementos) {
                datosPagina = new ArrayList<>();
            } else {
                datosPagina = todosLosHechos.subList(indiceInicio, indiceFin);
            }
            
            return new RespuestaPaginadaDTO<>(datosPagina, pagina, tamanioPagina, totalElementos);
            
        } catch (Exception e) {
            logger.error("Error al obtener hechos paginados del proxy demo: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener hechos paginados del proxy demo: " + e.getMessage(), e);
        }
    }
}

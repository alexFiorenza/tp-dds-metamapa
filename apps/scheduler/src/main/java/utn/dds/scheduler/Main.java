package utn.dds.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.scheduler.config.SchedulerConfig;
import utn.dds.scheduler.service.HttpSchedulerService;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            SchedulerConfig config = SchedulerConfig.fromEnvironment();
            
            logger.info("=== MetaMapa Scheduler ===");
            logger.info("Configuración cargada desde variables de entorno");
            
            HttpSchedulerService schedulerService = new HttpSchedulerService(
                config.getTargetUrl(),
                config.getIntervalSeconds(),
                config.getHttpMethod()
            );
            
            // Agregar shutdown hook para limpieza
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Recibida señal de terminación, cerrando scheduler...");
                schedulerService.stop();
            }));
            
            schedulerService.start();
            
            // Mantener el programa ejecutándose
            Thread.currentThread().join();
            
        } catch (IllegalArgumentException e) {
            logger.error("Error de configuración: {}", e.getMessage());
            logger.error("Variables de entorno requeridas:");
            logger.error("  - TARGET_URL: URL del endpoint a llamar");
            logger.error("Variables opcionales:");
            logger.error("  - INTERVAL_SECONDS: Intervalo en segundos (default: 60)");
            logger.error("  - HTTP_METHOD: Método HTTP (default: GET)");
            System.exit(1);
        } catch (Exception e) {
            logger.error("Error al iniciar el scheduler: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
package utn.dds;


import io.javalin.Javalin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.controller.ControllerNormalizador;
import utn.dds.dto.HechoDTO;


//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    static ControllerNormalizador controller = new ControllerNormalizador();


    public static void main(String[] args) {

            try {
                logger.info("Iniciando servicio:");


                Javalin app = Javalin.create(config -> {
                    config.bundledPlugins.enableDevLogging();
                    config.jsonMapper(new io.javalin.json.JavalinJackson().updateMapper(mapper -> {
                        mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
                        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                    }));

                });




                app.get("/health", ctx -> ctx.result("OK"));
                app.get("/", ctx -> ctx.result("Fuentes Estáticas - MetaMapa"));
                app.post("/hechos", controller::normalizar);


                app.start(7099);

                logger.info("Servicio de normalizacion iniciado en puerto 7099");

            } catch (Exception e) {
                logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
                System.exit(1);
            }
        }

    }
/*
* ejemplo pruba postman post: http://localhost:7099/hechos
*
* {
  "titulo": "Incendio en zona de monte",
  "descripcion": "Se reporta un foco de fuego en las afueras de la ciudad",
  "categoria": "fire",
  "fechaAcontecimiento": "2025-08-28",
  "origen": "MANUAL",
  "contribuyenteNombre": "Juan Pérez",
  "tipo": "TEXTO",
  "longitud": -58.3816,
  "latitud": -34.6037,
  "fechaCarga": "2025-08-28T16:00:00",
  "estado": "ACTIVO",
  "etiquetas": ["emergencia", "incendio"],
  "uuid": "123e4567-e89b-12d3-a456-426614174000"
}
*
* */
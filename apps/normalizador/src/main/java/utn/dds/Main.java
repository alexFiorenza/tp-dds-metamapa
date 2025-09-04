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
                app.put("/hechos", controller::Normalizar);


                app.start(7099);

                logger.info("Servicio de normalizacion iniciado en puerto 7099");

            } catch (Exception e) {
                logger.error("Error al iniciar el servicio: {}", e.getMessage(), e);
                System.exit(1);
            }
        }

    }

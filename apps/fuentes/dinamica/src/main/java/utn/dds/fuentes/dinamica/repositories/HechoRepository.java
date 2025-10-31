package utn.dds.fuentes.dinamica.repositories;

import utn.dds.daos.DAOFactory;
import utn.dds.daos.IDAO;
import utn.dds.dominio.EstadoHecho;
import utn.dds.dominio.Hecho;
import utn.dds.dto.HechoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class HechoRepository {
    private final IDAO<HechoDTO> dao;
    private final IDAO<HechoDTO> hechoS3;

    private static final Logger loggerRepository = LoggerFactory.getLogger(HechoRepository.class);

    public HechoRepository(String daoType, Map<String, Object> daoConfig) {
        hechoS3 = DAOFactory.createDAO(HechoDTO.class, "s3", daoConfig);
        if ("filesystem".equals(daoType)) {
            //loggerRepository.info("Estoy dentro de HechoRepository");  -- Logger comentado para debuggeo.
            Map<String, Object> config = new java.util.HashMap<>();
            config.put("url", "src/main/resources/mocks/hechos.json");
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, config);
        } else if ("couchdb".equals(daoType)) {
            // Para CouchDB, crear una DB específica para hechos
            Map<String, Object> configHechos = new java.util.HashMap<>(daoConfig);
            String baseUrl = (String) configHechos.get("baseUrl");
            String dbPrefix = (String) configHechos.get("dbPrefix");

            // Crear URL completa: http://localhost:5984/dinamica_db_hechos
            String fullUrl = baseUrl.endsWith("/")
                ? baseUrl + dbPrefix + "_hechos"
                : baseUrl + "/" + dbPrefix + "_hechos";

            configHechos.put("url", fullUrl);
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, configHechos);
        } else {
            this.dao = DAOFactory.createDAO(HechoDTO.class, daoType, daoConfig);
        }
    }

    public List<Hecho> obtenerHechos() throws IOException {
        //
        List<HechoDTO> hechosDTO = dao.find();
        List<Hecho> hechos = hechosDTO.stream()
                .map(HechoDTO::toHecho)
                .collect(Collectors.toList());
        return hechos;
    }

    public Hecho buscarHecho(String titulo) throws IOException {
        List<Hecho> hechos = obtenerHechos();

        Optional<Hecho> encontrado = hechos.stream()
                .filter(s -> s.getTitulo().equals(titulo))
                .findFirst();

        if (encontrado.isPresent()) {
            Hecho hechoEncontrado = encontrado.get();
            return hechoEncontrado;
        } else {
            throw new NoSuchElementException("No se encontró el Hecho con titulo: " + titulo);
        }
    }

    // Funcion provisional para persistir los hechos
    public void saveProvisional(HechoDTO hechoDto) {
        if (hechoDto.getMultimedia() != null && !hechoDto.getMultimedia().isEmpty()) {
            List<String> multimediaUrls = new ArrayList<>();
            for (String contenidoBase64 : hechoDto.getMultimedia()) {
                // Si viene con prefijo data:<mime>;base64, lo quitamos
                String base64 = contenidoBase64;
                if (base64.contains(",")) {
                    base64 = base64.substring(base64.indexOf(',') + 1);
                }

                byte[] datosBinarios = Base64.getDecoder().decode(base64);
                String nombreArchivo = UUID.randomUUID().toString();

                long contentLength = datosBinarios.length;

                // Convierte el byte[] a InputStream
                try (ByteArrayInputStream bais = new ByteArrayInputStream(datosBinarios)) {
                    String url = hechoS3.uploadBinary(nombreArchivo, bais, contentLength);  // Solucionar esto y listo
                    multimediaUrls.add(url);
                } catch (Exception e) {
                    // manejar error/rollback según tu lógica (loguear, lanzar excepción, etc.)
                    throw new RuntimeException("Error subiendo multimedia a S3", e);
                }
            }
            hechoDto.setMultimedia(multimediaUrls);
        }
        dao.save(hechoDto);
    }

    public HechoDTO aportarHecho(HechoDTO hecho) throws IOException {
        // Asignar fecha de carga actual automáticamente en formato ISO-8601
        hecho.setFechaCarga(java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        saveProvisional(hecho);   // Funcion provisional para guardar los hechos en couchdb con multimedia en minio s3
        return hecho;
    }

    public Hecho cambiarEstado(Hecho hecho) throws IOException {
        if(hecho.getEstado()== EstadoHecho.ACTIVO){
            hecho.ocultar();
        } else { hecho.activar(); }
        return hecho;
    }
} 

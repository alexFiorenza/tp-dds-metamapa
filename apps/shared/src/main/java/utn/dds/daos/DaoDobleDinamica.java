package utn.dds.daos;
import utn.dds.daos.CouchDB;
import utn.dds.daos.S3;
import utn.dds.dominio.Hecho;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

public class DaoDobleDinamica<T> implements IDAO<T>{
    private final CouchDB couchDBDao;
    private final S3 s3Dao;

    public DaoDobleDinamica(CouchDB couchDBDao, S3 s3Dao) {
        this.couchDBDao = couchDBDao;
        this.s3Dao = s3Dao;
    }

    @Override
    public void save(Hecho hecho){ // Fijarnos si le pasamos un hecho o hechodto
        if (hecho.getMultimedia() != null && !hecho.getMultimedia().isEmpty()) {
            List<String> multimediaUrls = new ArrayList<>();
            for (String contenidoBase64 : hecho.getMultimedia()) {
                // Asumimos que el string de entrada es Base64 y lo decodificamos a un array de bytes.
                byte[] datosBinarios = Base64.getDecoder().decode(contenidoBase64);
                String nombreArchivo = UUID.randomUUID().toString();

                // Subimos los datos binarios a S3 usando uploadBinary y obtenemos la URL.
                String url = s3Dao.uploadBinary(nombreArchivo, datosBinarios); // falta un parametro
                multimediaUrls.add(url);
            }
            // Actualizamos la lista de multimedia del hecho con las nuevas URLs.
            hecho.setMultimedia(multimediaUrls);
        }
        // Guardamos el objeto Hecho (con las URLs si correspondía) en CouchDB.
        couchDBDao.save(hecho);
    }

    // Estas no las usamos
    @Override
    public InputStream read(){
        throw new UnsupportedOperationException("CouchDB no soporta operaciones de read.");
    }
    @Override
    public InputStream read(String path){
        throw new UnsupportedOperationException("CouchDB no soporta operaciones de read.");
    }
    @Override
    public List<T> find(){
        throw new UnsupportedOperationException("CouchDB no soporta operaciones de find.");
    }

    @Override
    public void saveAll(List<T> objects){
        throw new UnsupportedOperationException("CouchDB no soporta operaciones de saveAll.");
    }

    @Override
    public void addAll(List<T> objects){
        throw new UnsupportedOperationException("CouchDB no soporta operaciones de readAll.");
    }
}

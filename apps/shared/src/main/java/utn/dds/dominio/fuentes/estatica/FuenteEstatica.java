package utn.dds.dominio.fuentes.estatica;

import utn.dds.dominio.Hecho;
import utn.dds.dominio.fuentes.FuenteDeDatos;
import utn.dds.dominio.fuentes.TipoFuente;
import utn.dds.dominio.fuentes.estatica.strategies.ProcesadorStrategy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FuenteEstatica implements FuenteDeDatos {
    private final InputStream input;
    private final ProcesadorStrategy procesador;

    public FuenteEstatica(InputStream input, ProcesadorStrategy procesador) {
        this.input = input;
        this.procesador = procesador;
    }

    @Override
    public List<Hecho> obtenerHechos() {
        List<Hecho> hechos = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
            String linea;
            while ((linea = reader.readLine()) != null) {
                Hecho hecho = procesador.procesar(linea);
                if (hecho != null) {
                    hechos.add(hecho);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error al leer la fuente estática", e);
        }
        return hechos;
    }

    @Override
    public TipoFuente tipo() {
        return TipoFuente.ESTATICA;
    }
} 
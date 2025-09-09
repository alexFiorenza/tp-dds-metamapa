package utn.dds.normalizaciones;

import utn.dds.dto.HechoDTO;

import java.text.Normalizer;
import java.util.HashMap;
import java.util.Map;

public class NormalizadorCategoria implements Normalizador {
    private static final Map<String, String> sinonimos = new HashMap<>();

    static {
        // Se carga una vez cuando la clase se usa por primera vez
        sinonimos.put("fuego", "Incendio Forestal");
        sinonimos.put("fire", "Incendio Forestal");
        sinonimos.put("forest fire", "Incendio Forestal");
        sinonimos.put("incendio monte", "Incendio Forestal");

        sinonimos.put("incendio urbano", "Incendio Urbano");
        sinonimos.put("house fire", "Incendio Urbano");
        sinonimos.put("building fire", "Incendio Urbano");

        sinonimos.put("inundacion", "Inundación");
        sinonimos.put("flood", "Inundación");
    }

    public static String buscar(String valor) {
        return sinonimos.getOrDefault(limpiar(valor), "Desconocida");
    }

    private static String limpiar(String input) {
        return Normalizer.normalize(input.toLowerCase().trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    @Override
    public HechoDTO normalizar(HechoDTO hecho) {
        if (hecho == null || hecho.getCategoria() == null) return hecho;

        String valor = hecho.getCategoria();
        String categoriaNormalizada = sinonimos.getOrDefault(limpiar(valor), "Desconocida");

        hecho.setCategoria(categoriaNormalizada);
        return hecho;
    }
}

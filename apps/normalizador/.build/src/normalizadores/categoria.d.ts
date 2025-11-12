import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion } from '../types.js';
/**
 * Normalizador de categorías: mapea variaciones de texto a categorías estándar
 * usando mapeo exacto, regex y fuzzy matching (Levenshtein).
 */
export declare class CategoriaNormalizador {
    readonly nombre = "CategoriaNormalizador";
    readonly nivel: "ADVERTENCIA";
    normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion;
    /**
     * Encuentra la categoría más similar usando distancia de Levenshtein
     */
    private encontrarMasSimilar;
}
//# sourceMappingURL=categoria.d.ts.map
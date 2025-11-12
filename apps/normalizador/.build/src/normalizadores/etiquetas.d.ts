import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion } from '../types.js';
/**
 * Normalizador de etiquetas: convierte a lowercase, elimina duplicados,
 * filtra vacías y ordena alfabéticamente.
 */
export declare class EtiquetasNormalizador {
    readonly nombre = "EtiquetasNormalizador";
    readonly nivel: "OPCIONAL";
    normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion;
}
//# sourceMappingURL=etiquetas.d.ts.map
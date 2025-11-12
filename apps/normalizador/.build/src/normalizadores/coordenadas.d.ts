import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion } from '../types.js';
/**
 * Normalizador de coordenadas: valida rangos geográficos y redondea a precisión razonable.
 * Latitud válida: [-90, 90]
 * Longitud válida: [-180, 180]
 * Precisión: 6 decimales (~11cm)
 */
export declare class CoordenadasNormalizador {
    readonly nombre = "CoordenadasNormalizador";
    readonly nivel: "CRITICO";
    normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion;
}
//# sourceMappingURL=coordenadas.d.ts.map
import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion } from '../types.js';
/**
 * Normalizador de texto: limpia espacios, saltos de línea y formato general
 * de los campos de texto del hecho.
 */
export declare class TextoNormalizador {
    readonly nombre = "TextoNormalizador";
    readonly nivel: "OPCIONAL";
    normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion;
    /**
     * Normaliza un texto eliminando espacios extra y saltos de línea excesivos
     */
    private normalizarTexto;
}
//# sourceMappingURL=texto.d.ts.map
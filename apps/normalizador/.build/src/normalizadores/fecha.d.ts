import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion } from '../types.js';
/**
 * Normalizador de fechas: convierte fechas de diferentes formatos a formato ISO estándar
 * y elimina información de timezone.
 */
export declare class FechaNormalizador {
    readonly nombre = "FechaNormalizador";
    readonly nivel: "ADVERTENCIA";
    normalizar(hecho: HechoDTO, config: Required<ConfiguracionNormalizacion>): ResultadoNormalizacion;
    /**
     * Normaliza una fecha individual
     */
    private normalizarFecha;
}
//# sourceMappingURL=fecha.d.ts.map
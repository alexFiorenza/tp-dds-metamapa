import type { HechoDTO, ConfiguracionNormalizacion, ResultadoNormalizacion } from './types.js';
/**
 * Pipeline de normalización que ejecuta múltiples normalizadores en secuencia.
 *
 * Orden de ejecución:
 * 1. TextoNormalizador - Limpia espacios y formato
 * 2. CategoriaNormalizador - Mapea categorías
 * 3. FechaNormalizador - Normaliza fechas
 * 4. CoordenadasNormalizador - Valida y redondea coordenadas
 * 5. EtiquetasNormalizador - Normaliza etiquetas
 */
export declare class NormalizadorPipeline {
    private pipeline;
    private config;
    constructor(config?: ConfiguracionNormalizacion);
    /**
     * Normaliza un hecho ejecutando todos los normalizadores habilitados
     */
    normalizar(hecho: HechoDTO): ResultadoNormalizacion;
    /**
     * Verifica si un normalizador está habilitado según la configuración
     */
    private estaHabilitado;
}
//# sourceMappingURL=pipeline.d.ts.map
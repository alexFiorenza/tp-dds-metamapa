import type { NormalizarRequest, NormalizarResponse } from '../types.js';
/**
 * Función principal de normalización - agnóstica del transporte (HTTP/Lambda)
 * Esta función puede ser llamada desde cualquier handler
 */
export declare function normalizarHechos(request: NormalizarRequest): NormalizarResponse;
/**
 * Obtener categorías disponibles
 */
export declare function obtenerCategorias(): {
    categorias: string[];
};
//# sourceMappingURL=normalizar.d.ts.map
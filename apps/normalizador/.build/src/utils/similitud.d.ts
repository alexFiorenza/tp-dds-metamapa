/**
 * Calcula la distancia de Levenshtein entre dos strings.
 * Representa el número mínimo de operaciones (inserción, eliminación, sustitución)
 * necesarias para transformar un string en otro.
 *
 * @param str1 Primer string
 * @param str2 Segundo string
 * @returns Distancia de Levenshtein (0 = idénticos)
 */
export declare function levenshteinDistance(str1: string, str2: string): number;
/**
 * Calcula la similitud entre dos strings (0-1)
 * 1 = idénticos, 0 = completamente diferentes
 *
 * @param str1 Primer string
 * @param str2 Segundo string
 * @returns Similitud entre 0 y 1
 */
export declare function calcularSimilitud(str1: string, str2: string): number;
//# sourceMappingURL=similitud.d.ts.map
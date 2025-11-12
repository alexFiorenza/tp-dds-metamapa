// src/utils/similitud.ts
/**
 * Calcula la distancia de Levenshtein entre dos strings.
 * Representa el número mínimo de operaciones (inserción, eliminación, sustitución)
 * necesarias para transformar un string en otro.
 *
 * @param str1 Primer string
 * @param str2 Segundo string
 * @returns Distancia de Levenshtein (0 = idénticos)
 */
export function levenshteinDistance(str1, str2) {
    const m = str1.length;
    const n = str2.length;
    // Crear matriz de programación dinámica
    const dp = Array.from({ length: m + 1 }, () => Array(n + 1).fill(0));
    for (let i = 0; i <= m; i++) {
        dp[i][0] = i;
    }
    for (let j = 0; j <= n; j++) {
        dp[0][j] = j;
    }
    for (let i = 1; i <= m; i++) {
        for (let j = 1; j <= n; j++) {
            if (str1[i - 1] === str2[j - 1]) {
                dp[i][j] = dp[i - 1][j - 1];
            }
            else {
                dp[i][j] = 1 + Math.min(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1]);
            }
        }
    }
    return dp[m][n];
}
/**
 * Calcula la similitud entre dos strings (0-1)
 * 1 = idénticos, 0 = completamente diferentes
 *
 * @param str1 Primer string
 * @param str2 Segundo string
 * @returns Similitud entre 0 y 1
 */
export function calcularSimilitud(str1, str2) {
    const distancia = levenshteinDistance(str1, str2);
    const maxLength = Math.max(str1.length, str2.length);
    if (maxLength === 0) {
        return 1;
    }
    return 1 - (distancia / maxLength);
}
//# sourceMappingURL=similitud.js.map
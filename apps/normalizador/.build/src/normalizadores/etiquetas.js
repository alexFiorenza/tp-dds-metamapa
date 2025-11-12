/**
 * Normalizador de etiquetas: convierte a lowercase, elimina duplicados,
 * filtra vacías y ordena alfabéticamente.
 */
export class EtiquetasNormalizador {
    nombre = 'EtiquetasNormalizador';
    nivel = 'OPCIONAL';
    normalizar(hecho, config) {
        const cambios = [];
        const hechoNormalizado = { ...hecho };
        if (hecho.etiquetas && hecho.etiquetas.length > 0) {
            // Normalizar: lowercase, trim, eliminar duplicados, ordenar
            const etiquetasNormalizadas = [...new Set(hecho.etiquetas
                    .map(e => e.toLowerCase().trim()) // lowercase + trim
                    .filter(e => e.length > 0) // quitar vacías
                )].sort(); // ordenar alfabéticamente
            // Verificar si hubo cambios
            const hayCambios = etiquetasNormalizadas.length !== hecho.etiquetas.length ||
                !etiquetasNormalizadas.every((e, i) => e === hecho.etiquetas[i]?.toLowerCase()?.trim());
            if (hayCambios) {
                hechoNormalizado.etiquetas = etiquetasNormalizadas;
                cambios.push({
                    campo: 'etiquetas',
                    valorOriginal: hecho.etiquetas,
                    valorNormalizado: etiquetasNormalizadas,
                    metodo: 'lowercase_dedup_sort'
                });
            }
        }
        return {
            hechoNormalizado,
            cambios,
            advertencias: [],
            rechazado: false
        };
    }
}
//# sourceMappingURL=etiquetas.js.map
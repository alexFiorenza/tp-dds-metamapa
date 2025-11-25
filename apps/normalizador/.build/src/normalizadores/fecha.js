/**
 * Normalizador de fechas: convierte fechas de diferentes formatos a formato ISO estándar
 * y elimina información de timezone.
 */
export class FechaNormalizador {
    nombre = 'FechaNormalizador';
    nivel = 'ADVERTENCIA';
    normalizar(hecho, config) {
        const cambios = [];
        const advertencias = [];
        const hechoNormalizado = { ...hecho };
        // Normalizar fechaAcontecimiento
        if (hecho.fechaAcontecimiento) {
            const resultado = this.normalizarFecha(hecho.fechaAcontecimiento, 'fechaAcontecimiento');
            if (resultado.cambio) {
                hechoNormalizado.fechaAcontecimiento = resultado.fechaNormalizada;
                cambios.push(resultado.cambio);
            }
            if (resultado.advertencia) {
                advertencias.push(resultado.advertencia);
            }
        }
        // Normalizar fechaCarga
        if (hecho.fechaCarga) {
            const resultado = this.normalizarFecha(hecho.fechaCarga, 'fechaCarga');
            if (resultado.cambio) {
                hechoNormalizado.fechaCarga = resultado.fechaNormalizada;
                cambios.push(resultado.cambio);
            }
            if (resultado.advertencia) {
                advertencias.push(resultado.advertencia);
            }
        }
        return {
            hechoNormalizado,
            cambios,
            advertencias,
            rechazado: false
        };
    }
    /**
     * Normaliza una fecha individual
     */
    normalizarFecha(fecha, campo) {
        const fechaOriginal = fecha;
        // Ya está en formato ISO correcto (YYYY-MM-DD o YYYY-MM-DDTHH:mm:ss)
        if (/^\d{4}-\d{2}-\d{2}$/.test(fecha) || /^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(fecha)) {
            return { fechaNormalizada: fecha };
        }
        // Contiene timezone (.000Z, Z, o +00:00)
        if (fecha.includes('T') && (fecha.endsWith('Z') || /[+-]\d{2}:\d{2}$/.test(fecha) || /\.\d{3}Z$/.test(fecha))) {
            // Extraer solo la parte de fecha o fecha-hora sin milisegundos ni timezone
            let fechaNormalizada = fecha;
            // Quitar milisegundos (.000)
            if (fecha.includes('.')) {
                fechaNormalizada = fecha.substring(0, fecha.indexOf('.'));
            }
            // Quitar Z
            else if (fecha.endsWith('Z')) {
                fechaNormalizada = fecha.substring(0, fecha.length - 1);
            }
            // Quitar offset (+00:00 o -03:00)
            else if (/[+-]\d{2}:\d{2}$/.test(fecha)) {
                fechaNormalizada = fecha.substring(0, fecha.lastIndexOf('+') || fecha.lastIndexOf('-'));
            }
            return {
                fechaNormalizada,
                cambio: {
                    campo,
                    valorOriginal: fechaOriginal,
                    valorNormalizado: fechaNormalizada,
                    metodo: 'quitar_timezone'
                }
            };
        }
        // Formato no reconocido - devolver sin cambios pero advertir
        return {
            fechaNormalizada: fecha,
            advertencia: {
                tipo: 'fecha_formato_desconocido',
                mensaje: `Formato de fecha no reconocido en ${campo}: ${fecha}`,
                severidad: 'warning'
            }
        };
    }
}
//# sourceMappingURL=fecha.js.map
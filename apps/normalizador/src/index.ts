// src/index.ts
import { createServer, IncomingMessage, ServerResponse } from 'http'
import { NormalizadorPipeline } from './pipeline.js'
import type { NormalizarRequest, NormalizarResponse, HechoRechazado } from './types.js'

const PORT = process.env.PORT || 3000

/**
 * Parsea el body JSON de una request HTTP
 */
async function parseBody(req: IncomingMessage): Promise<any> {
  return new Promise((resolve, reject) => {
    let body = ''
    req.on('data', chunk => {
      body += chunk.toString()
    })
    req.on('end', () => {
      try {
        resolve(JSON.parse(body))
      } catch (e) {
        reject(new Error('JSON inválido'))
      }
    })
    req.on('error', reject)
  })
}

/**
 * Configura headers CORS
 */
function setCorsHeaders(res: ServerResponse) {
  res.setHeader('Access-Control-Allow-Origin', '*')
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, OPTIONS')
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type')
}

/**
 * Handler principal del servidor HTTP
 */
const server = createServer(async (req: IncomingMessage, res: ServerResponse) => {
  setCorsHeaders(res)

  // Handle CORS preflight
  if (req.method === 'OPTIONS') {
    res.writeHead(200)
    res.end()
    return
  }

  // Health check
  if (req.method === 'GET' && (req.url === '/' || req.url === '/health')) {
    res.writeHead(200, { 'Content-Type': 'application/json' })
    res.end(JSON.stringify({
      status: 'ok',
      service: 'metamapa-normalizador',
      version: '1.0.0',
      uptime: process.uptime(),
      timestamp: new Date().toISOString()
    }))
    return
  }

  // Endpoint de normalización
  if (req.method === 'POST' && (req.url === '/' || req.url === '/normalizar')) {
    const startTime = Date.now()

    try {
      // Parsear body
      const body = await parseBody(req) as NormalizarRequest
      const { hechos, config } = body

      // Validaciones
      if (!Array.isArray(hechos)) {
        res.writeHead(400, { 'Content-Type': 'application/json' })
        res.end(JSON.stringify({ error: 'Se esperaba un array de hechos' }))
        return
      }

      if (hechos.length === 0) {
        res.writeHead(400, { 'Content-Type': 'application/json' })
        res.end(JSON.stringify({ error: 'El array de hechos está vacío' }))
        return
      }

      if (hechos.length > 1000) {
        res.writeHead(400, { 'Content-Type': 'application/json' })
        res.end(JSON.stringify({
          error: 'Máximo 1000 hechos por request. Procesar en lotes.'
        }))
        return
      }

      // Log
      console.log(`[${new Date().toISOString()}] Procesando ${hechos.length} hechos`)

      // Crear pipeline y normalizar
      const pipeline = new NormalizadorPipeline(config)
      const hechosNormalizados = []
      const hechosRechazados: HechoRechazado[] = []
      const cambiosPorTipo: Record<string, number> = {}

      for (const hecho of hechos) {
        const resultado = pipeline.normalizar(hecho)

        if (resultado.rechazado) {
          hechosRechazados.push({
            hechoOriginal: hecho,
            motivo: resultado.motivoRechazo || 'Error desconocido',
            errores: resultado.advertencias.map(a => a.mensaje)
          })
        } else {
          hechosNormalizados.push(resultado.hechoNormalizado)

          // Contar cambios por tipo
          resultado.cambios.forEach(cambio => {
            cambiosPorTipo[cambio.campo] = (cambiosPorTipo[cambio.campo] || 0) + 1
          })
        }
      }

      const tiempoEjecucion = Date.now() - startTime

      // Log resultado
      console.log(`[${new Date().toISOString()}] Completado en ${tiempoEjecucion}ms - ${hechosNormalizados.length} normalizados, ${hechosRechazados.length} rechazados`)

      // Respuesta
      const response: NormalizarResponse = {
        hechosNormalizados,
        hechosRechazados,
        estadisticas: {
          totalProcesados: hechos.length,
          totalNormalizados: hechosNormalizados.length,
          totalRechazados: hechosRechazados.length,
          cambiosPorTipo,
          tiempoEjecucion
        }
      }

      res.writeHead(200, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify(response))

    } catch (error) {
      console.error('[Error]', error)
      res.writeHead(500, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify({
        error: error instanceof Error ? error.message : 'Error interno',
        timestamp: new Date().toISOString()
      }))
    }
    return
  }

  // Ruta no encontrada
  res.writeHead(404, { 'Content-Type': 'application/json' })
  res.end(JSON.stringify({ error: 'Ruta no encontrada' }))
})

// Iniciar servidor
server.listen(PORT, () => {
  console.log(`🚂 Normalizador Railway escuchando en puerto ${PORT}`)
  console.log(`📊 Endpoints disponibles:`)
  console.log(`   GET  /health     - Health check`)
  console.log(`   POST /normalizar - Normalizar hechos`)
})

// Manejo de señales de terminación
process.on('SIGTERM', () => {
  console.log('SIGTERM recibido, cerrando servidor...')
  server.close(() => {
    console.log('Servidor cerrado')
    process.exit(0)
  })
})

process.on('SIGINT', () => {
  console.log('SIGINT recibido, cerrando servidor...')
  server.close(() => {
    console.log('Servidor cerrado')
    process.exit(0)
  })
})

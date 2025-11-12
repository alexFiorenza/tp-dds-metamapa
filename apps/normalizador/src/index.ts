// src/index.ts
import { createServer, IncomingMessage, ServerResponse } from 'http'
import type { NormalizarRequest } from './types.js'

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

  // Endpoint de categorías disponibles
  if (req.method === 'GET' && req.url === '/categorias') {
    res.writeHead(200, { 'Content-Type': 'application/json' })
    const { obtenerCategorias } = await import('./core/normalizar.js')
    res.end(JSON.stringify(obtenerCategorias()))
    return
  }

  // Endpoint de normalización
  if (req.method === 'POST' && (req.url === '/' || req.url === '/normalizar')) {
    try {
      // Parsear body
      const body = await parseBody(req) as NormalizarRequest

      // Importar y ejecutar lógica de negocio
      const { normalizarHechos } = await import('./core/normalizar.js')
      const response = normalizarHechos(body)

      res.writeHead(200, { 'Content-Type': 'application/json' })
      res.end(JSON.stringify(response))

    } catch (error) {
      console.error('[Error]', error)
      const statusCode = error instanceof Error && error.message.includes('array') ? 400 : 500
      res.writeHead(statusCode, { 'Content-Type': 'application/json' })
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
  console.log(`   GET  /categorias - Obtener categorías disponibles`)
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

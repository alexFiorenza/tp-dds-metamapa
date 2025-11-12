// src/lambda.ts

import type { APIGatewayProxyEvent, APIGatewayProxyResult, Context } from 'aws-lambda'
import { normalizarHechos, obtenerCategorias } from './core/normalizar.js'
import type { NormalizarRequest } from './types.js'

/**
 * Handler para POST /normalizar
 * AWS Lambda invoca esta función cuando llega una request al endpoint
 */
export async function normalizarHandler(
  event: APIGatewayProxyEvent,
  context: Context
): Promise<APIGatewayProxyResult> {
  console.log('Lambda invocada - Normalizar', { requestId: context.awsRequestId })

  // Headers CORS
  const headers = {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'POST, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type'
  }

  if (event.httpMethod === 'OPTIONS') {
    return {
      statusCode: 200,
      headers,
      body: ''
    }
  }

  try {
    if (!event.body) {
      return {
        statusCode: 400,
        headers,
        body: JSON.stringify({ error: 'Body requerido' })
      }
    }

    const request: NormalizarRequest = JSON.parse(event.body)

    const response = normalizarHechos(request)

    return {
      statusCode: 200,
      headers,
      body: JSON.stringify(response)
    }

  } catch (error) {
    console.error('Error en Lambda:', error)

    const statusCode = error instanceof Error && error.message.includes('array') ? 400 : 500

    return {
      statusCode,
      headers,
      body: JSON.stringify({
        error: error instanceof Error ? error.message : 'Error interno',
        timestamp: new Date().toISOString()
      })
    }
  }
}

/**
 * Handler para GET /categorias
 * Retorna las categorías válidas configuradas
 */
export async function categoriasHandler(
  event: APIGatewayProxyEvent,
  context: Context
): Promise<APIGatewayProxyResult> {
  console.log('Lambda invocada - Categorias', { requestId: context.awsRequestId })

  const headers = {
    'Content-Type': 'application/json',
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET, OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type'
  }

  if (event.httpMethod === 'OPTIONS') {
    return {
      statusCode: 200,
      headers,
      body: ''
    }
  }

  try {
    const response = obtenerCategorias()

    return {
      statusCode: 200,
      headers,
      body: JSON.stringify(response)
    }

  } catch (error) {
    console.error('Error en Lambda:', error)

    return {
      statusCode: 500,
      headers,
      body: JSON.stringify({
        error: error instanceof Error ? error.message : 'Error interno',
        timestamp: new Date().toISOString()
      })
    }
  }
}

/**
 * Handler para GET /health
 */
export async function healthHandler(
  event: APIGatewayProxyEvent,
  context: Context
): Promise<APIGatewayProxyResult> {
  return {
    statusCode: 200,
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      status: 'ok',
      service: 'metamapa-normalizador-lambda',
      version: '1.0.0',
      timestamp: new Date().toISOString(),
      requestId: context.awsRequestId
    })
  }
}

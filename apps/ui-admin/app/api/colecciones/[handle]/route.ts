import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7006"

interface RouteContext {
  params: Promise<{
    handle: string
  }>
}

export async function GET(request: NextRequest, context: RouteContext) {
  try {
    const { handle } = await context.params

    const javaUrl = `${API_BASE_URL}/api/colecciones/${handle}`

    const response = await fetch(javaUrl, {
      cache: 'no-store' // Asegurar datos frescos en cada request
    })

    if (!response.ok) {
      throw new Error(`Error del backend Java: ${response.statusText}`)
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error en endpoint puente de colección:', error)
    return NextResponse.json(
      { error: 'Error al conectar con el backend' },
      { status: 500 }
    )
  }
}

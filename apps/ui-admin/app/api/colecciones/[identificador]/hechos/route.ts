import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7006"

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ identificador: string }> }
) {
  try {
    const { identificador } = await params
    const { searchParams } = new URL(request.url)

    // Pasar todos los parámetros de query al backend Java
    const javaUrl = new URL(`${API_BASE_URL}/api/colecciones/${identificador}/hechos`)
    searchParams.forEach((value, key) => {
      javaUrl.searchParams.append(key, value)
    })

    const response = await fetch(javaUrl.toString(), {
      cache: 'no-store' // Asegurar datos frescos en cada request
    })

    if (!response.ok) {
      throw new Error(`Error del backend Java: ${response.statusText}`)
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error en endpoint puente de colecciones:', error)
    return NextResponse.json(
      { error: 'Error al conectar con el backend' },
      { status: 500 }
    )
  }
}

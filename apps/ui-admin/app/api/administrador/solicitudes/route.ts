import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7006"

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url)

    // Obtener el token de autorización del header
    const authorization = request.headers.get('authorization')

    // Pasar todos los parámetros de query al backend Java
    const javaUrl = new URL(`${API_BASE_URL}/administrador/solicitudes`)
    searchParams.forEach((value, key) => {
      javaUrl.searchParams.append(key, value)
    })

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    if (authorization) {
      headers['Authorization'] = authorization
    }

    const response = await fetch(javaUrl.toString(), {
      headers,
      cache: 'no-store'
    })

    if (!response.ok) {
      const errorText = await response.text()
      return NextResponse.json(
        { error: errorText || 'Error al obtener solicitudes' },
        { status: response.status }
      )
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error en endpoint puente de solicitudes administrativas:', error)
    return NextResponse.json(
      { error: 'Error al conectar con el backend' },
      { status: 500 }
    )
  }
}

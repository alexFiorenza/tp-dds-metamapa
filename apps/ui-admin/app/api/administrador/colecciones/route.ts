import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7006"

export async function GET(request: NextRequest) {
  try {
    const { searchParams } = new URL(request.url)
    const token = request.headers.get('Authorization')

    // Pasar todos los parámetros de query al backend Java
    const javaUrl = new URL(`${API_BASE_URL}/administrador/coleccion`)
    searchParams.forEach((value, key) => {
      javaUrl.searchParams.append(key, value)
    })

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    if (token) {
      headers['Authorization'] = token
    }

    const response = await fetch(javaUrl.toString(), {
      cache: 'no-store',
      headers
    })

    if (!response.ok) {
      throw new Error(`Error del backend Java: ${response.statusText}`)
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error en endpoint de colecciones:', error)
    return NextResponse.json(
      { error: 'Error al conectar con el backend' },
      { status: 500 }
    )
  }
}

export async function POST(request: NextRequest) {
  try {
    const token = request.headers.get('Authorization')
    const body = await request.json()

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    if (token) {
      headers['Authorization'] = token
    }

    const response = await fetch(`${API_BASE_URL}/administrador/coleccion`, {
      method: 'POST',
      headers,
      body: JSON.stringify(body),
      cache: 'no-store'
    })

    if (!response.ok) {
      const errorText = await response.text()
      throw new Error(`Error del backend Java: ${response.statusText} - ${errorText}`)
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error al crear colección:', error)
    return NextResponse.json(
      { error: error instanceof Error ? error.message : 'Error al crear colección' },
      { status: 500 }
    )
  }
}

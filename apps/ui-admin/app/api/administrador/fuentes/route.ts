import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7006"

export async function GET(request: NextRequest) {
  try {
    const token = request.headers.get('Authorization')

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    if (token) {
      headers['Authorization'] = token
    }

    const response = await fetch(`${API_BASE_URL}/administrador/fuentes`, {
      cache: 'no-store',
      headers
    })

    if (!response.ok) {
      throw new Error(`Error del backend Java: ${response.statusText}`)
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error en endpoint de fuentes:', error)
    return NextResponse.json(
      { error: 'Error al conectar con el backend' },
      { status: 500 }
    )
  }
}

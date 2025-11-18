import { NextRequest, NextResponse } from 'next/server'

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:7006"

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ uuid: string }> }
) {
  try {
    const { uuid } = await params

    // Obtener el token de autorización del header
    const authorization = request.headers.get('authorization')

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
    }

    if (authorization) {
      headers['Authorization'] = authorization
    }

    const response = await fetch(`${API_BASE_URL}/administrador/solicitud/${uuid}/rechazar`, {
      method: 'PUT',
      headers,
      cache: 'no-store'
    })

    if (!response.ok) {
      const errorText = await response.text()
      return NextResponse.json(
        { error: errorText || 'Error al rechazar solicitud' },
        { status: response.status }
      )
    }

    return new NextResponse(null, { status: 200 })
  } catch (error) {
    console.error('Error en endpoint puente para rechazar solicitud:', error)
    return NextResponse.json(
      { error: 'Error al conectar con el backend' },
      { status: 500 }
    )
  }
}

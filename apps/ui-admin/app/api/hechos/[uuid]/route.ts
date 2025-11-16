import { NextRequest, NextResponse } from 'next/server'

const FUENTE_DINAMICA_URL = process.env.NEXT_PUBLIC_FUENTE_DINAMICA_URL || "http://localhost:7002"

export async function PATCH(
  request: NextRequest,
  { params }: { params: { uuid: string } }
) {
  try {
    const { uuid } = params

    // Obtener el token de autorización de los headers
    const authHeader = request.headers.get('Authorization')

    if (!authHeader) {
      return NextResponse.json(
        { error: 'Se requiere autenticación' },
        { status: 401 }
      )
    }

    // Obtener el FormData del request
    const formData = await request.formData()

    // Hacer el PATCH al backend de la fuente dinámica
    const response = await fetch(`${FUENTE_DINAMICA_URL}/hechos/${uuid}`, {
      method: 'PATCH',
      headers: {
        'Authorization': authHeader,
      },
      body: formData,
    })

    if (!response.ok) {
      const errorText = await response.text()
      return NextResponse.json(
        { error: errorText || 'Error al actualizar el hecho' },
        { status: response.status }
      )
    }

    const data = await response.json()
    return NextResponse.json(data)
  } catch (error) {
    console.error('Error en endpoint de actualización de hechos:', error)
    return NextResponse.json(
      { error: 'Error interno al actualizar el hecho' },
      { status: 500 }
    )
  }
}

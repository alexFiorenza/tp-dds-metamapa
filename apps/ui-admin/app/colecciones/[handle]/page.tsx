import { ApiClient } from "@/lib/api-client"
import { ColeccionDetailView } from "@/components/coleccion-detail-view"
import { notFound } from "next/navigation"

interface ColeccionPageProps {
  params: Promise<{
    handle: string
  }>
}

export default async function ColeccionPage({ params }: ColeccionPageProps) {
  const { handle } = await params
  
  try {
    // Obtener la colección
    const coleccion = await ApiClient.obtenerColeccionPublica(handle)
    
    // Obtener los hechos de la colección
    const respuesta = await ApiClient.obtenerHechosDeColeccion(handle, {
      pagina: 0,
      tamanioPagina: 10,
    })

    // Función para fetch de páginas
    const fetchPage = async (page: number) => {
      'use server'
      return await ApiClient.obtenerHechosDeColeccion(handle, {
        pagina: page,
        tamanioPagina: 10,
      })
    }

    return (
      <div className="h-full w-full">
        <ColeccionDetailView 
          coleccion={coleccion}
          initialData={respuesta} 
          fetchPage={fetchPage}
          backUrl="/colecciones"
          backLabel="Volver a Colecciones"
        />
      </div>
    )
  } catch (error) {
    notFound()
  }
}

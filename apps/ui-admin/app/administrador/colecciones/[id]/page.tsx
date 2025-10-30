import { ApiClient } from "@/lib/api-client"
import { ColeccionDetailView } from "@/components/coleccion-detail-view"

interface Props {
  params: Promise<{ id: string }>
}

export default async function ColeccionDetailPage({ params }: Props) {
  const { id } = await params

  const coleccion = await ApiClient.obtenerColeccionPublica(id)
  const respuestaHechos = await ApiClient.obtenerHechosDeColeccion(id, { pagina: 0, tamanioPagina: 10 })

  // Función para fetch de páginas
  const fetchPage = async (page: number) => {
    'use server'
    return await ApiClient.obtenerHechosDeColeccion(id, { pagina: page, tamanioPagina: 10 })
  }

  return (
    <div className="h-[calc(100vh-4rem)]">
      <ColeccionDetailView
        coleccion={coleccion}
        initialData={respuestaHechos}
        fetchPage={fetchPage}
        backUrl="/administrador/colecciones"
        backLabel="Volver a Colecciones"
      />
    </div>
  )
}

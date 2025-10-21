import { ApiClient } from "@/lib/api-client"
import { getAuthToken } from "@/lib/auth-helpers"
import { ColeccionDetailView } from "@/components/coleccion-detail-view"

interface Props {
  params: Promise<{ id: string }>
}

export default async function ColeccionDetailPage({ params }: Props) {
  const { id } = await params
  const token = await getAuthToken()

  const coleccion = await ApiClient.obtenerColeccion(id, token)
  const respuestaHechos = await ApiClient.obtenerHechosDeColeccionAdmin(id, { pagina: 0, tamanioPagina: 10 }, token)

  // Función para fetch de páginas
  const fetchPage = async (page: number) => {
    'use server'
    const token = await getAuthToken()
    return await ApiClient.obtenerHechosDeColeccionAdmin(id, { pagina: page, tamanioPagina: 10 }, token)
  }

  return (
    <div className="h-[calc(100vh-4rem)]">
      <ColeccionDetailView
        coleccion={coleccion}
        initialData={respuestaHechos}
        fetchPage={fetchPage}
      />
    </div>
  )
}

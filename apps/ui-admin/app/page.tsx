import { ApiClient } from "@/lib/api-client"
import { HechosMapView } from "@/components/hechos-map-view"

export default async function HomePage() {
  const respuesta = await ApiClient.obtenerHechos({ pagina: 0, tamanioPagina: 10 })

  // Función para fetch de páginas (se ejecutará en el cliente)
  const fetchPage = async (page: number) => {
    'use server'
    return await ApiClient.obtenerHechos({ pagina: page, tamanioPagina: 10 })
  }

  return (
    <div className="h-[calc(100vh-4rem)]">
      <HechosMapView initialData={respuesta} fetchPage={fetchPage} />
    </div>
  )
}

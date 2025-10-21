import { ApiClient } from "@/lib/api-client"
import { HechosMapView } from "@/components/hechos-map-view"

interface HechosPageProps {
  searchParams: Promise<{
    categoria?: string
    titulo?: string
  }>
}

export default async function HechosPage({ searchParams }: HechosPageProps) {
  const params = await searchParams
  const respuesta = await ApiClient.obtenerHechos({
    pagina: 0,
    tamanioPagina: 10,
    categoria: params.categoria,
    titulo: params.titulo,
  })

  // Función para fetch de páginas
  const fetchPage = async (page: number) => {
    'use server'
    return await ApiClient.obtenerHechos({
      pagina: page,
      tamanioPagina: 10,
      categoria: params.categoria,
      titulo: params.titulo,
    })
  }

  return (
    <div className="h-[calc(100vh-4rem)]">
      <HechosMapView initialData={respuesta} fetchPage={fetchPage} />
    </div>
  )
}

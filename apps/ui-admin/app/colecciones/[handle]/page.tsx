import { ApiClient } from "@/lib/api-client"
import { ColeccionPageClient } from "./page-client"
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

    // Pasar el handle y la colección al componente cliente
    return <ColeccionPageClient handle={handle} coleccion={coleccion} />
  } catch (error) {
    notFound()
  }
}

import { ApiClient } from "@/lib/api-client"
import { ColeccionesPageClient } from "./page-client"

export default async function ColeccionesPage() {
  const respuesta = await ApiClient.obtenerColeccionesPublicas(0, 20)

  return <ColeccionesPageClient colecciones={respuesta.datos} totalColecciones={respuesta.totalElementos} />
}

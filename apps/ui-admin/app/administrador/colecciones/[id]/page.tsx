import { ApiClient } from "@/lib/api-client"
import { getAuthToken } from "@/lib/auth-helpers"
import { SidebarLayout } from "@/components/sidebar-layout"
import { HechoCard } from "@/components/hecho-card"
import { MapWrapper } from "@/components/map-wrapper"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { ArrowLeft, Folder, Edit, Trash2 } from "lucide-react"
import Link from "next/link"

interface Props {
  params: Promise<{ id: string }>
}

export default async function ColeccionDetailPage({ params }: Props) {
  const { id } = await params
  const token = await getAuthToken()

  const coleccion = await ApiClient.obtenerColeccion(id, token)
  const respuestaHechos = await ApiClient.obtenerHechosDeColeccionAdmin(id, { tamanioPagina: 100 }, token)

  return (
    <SidebarLayout
      sidebar={
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="p-4 border-b border-border">
            <Link href="/administrador/colecciones">
              <Button variant="ghost" size="sm" className="mb-3">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Volver a Colecciones
              </Button>
            </Link>

            <div className="flex items-start gap-3 mb-4">
              <div className="p-3 bg-primary/10 rounded-lg">
                <Folder className="h-6 w-6 text-primary" />
              </div>
              <div className="flex-1 min-w-0">
                <h1 className="text-2xl font-bold mb-1">{coleccion.titulo}</h1>
                <p className="text-sm text-muted-foreground">{coleccion.descripcion}</p>
              </div>
            </div>

            {/* Metadata */}
            <Card className="p-3 mb-3">
              <div className="space-y-2 text-sm">
                <div>
                  <span className="font-semibold">Handle:</span>{" "}
                  <span className="text-muted-foreground">{coleccion.handle}</span>
                </div>
                <div>
                  <span className="font-semibold">Hechos:</span>{" "}
                  <span className="text-muted-foreground">{respuestaHechos.totalElementos}</span>
                </div>
              </div>
            </Card>

            {/* Criterios de pertenencia */}
            {coleccion.criteriosDePertenencia.length > 0 && (
              <div className="mb-3">
                <h3 className="text-sm font-semibold mb-2">Criterios de Pertenencia</h3>
                <div className="flex flex-wrap gap-1">
                  {coleccion.criteriosDePertenencia.map((criterio, idx) => (
                    <Badge key={idx} variant="outline" className="text-xs">
                      {criterio.categoria}
                    </Badge>
                  ))}
                </div>
              </div>
            )}

            {/* Actions */}
            <div className="flex gap-2">
              <Button variant="outline" size="sm" className="flex-1">
                <Edit className="h-4 w-4 mr-2" />
                Editar
              </Button>
              <Button variant="outline" size="sm" className="flex-1 text-destructive hover:text-destructive">
                <Trash2 className="h-4 w-4 mr-2" />
                Eliminar
              </Button>
            </div>
          </div>

          {/* List of hechos */}
          <div className="flex-1 overflow-y-auto p-4">
            <h2 className="text-sm font-semibold mb-3 text-muted-foreground uppercase tracking-wide">
              Hechos de esta colección ({respuestaHechos.totalElementos})
            </h2>
            <div className="space-y-3">
              {respuestaHechos.datos.map((hecho) => (
                <HechoCard key={hecho.uuid} hecho={hecho} />
              ))}
            </div>
            {respuestaHechos.datos.length === 0 && (
              <div className="text-center text-muted-foreground py-8">
                <Folder className="h-12 w-12 mx-auto mb-3 opacity-50" />
                <p>No hay hechos en esta colección</p>
              </div>
            )}
          </div>
        </div>
      }
    >
      <MapWrapper hechos={respuestaHechos.datos} />
    </SidebarLayout>
  )
}

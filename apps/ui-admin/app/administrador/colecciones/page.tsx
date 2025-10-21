import { ApiClient } from "@/lib/api-client"
import { getAuthToken } from "@/lib/auth-helpers"
import { MapWrapper } from "@/components/map-wrapper"
import { Button, Card, CardBody, Chip } from "@heroui/react"
import { ArrowLeft, Folder, Plus, Eye, Edit, Trash2 } from "lucide-react"
import Link from "next/link"

export default async function AdminColeccionesPage() {
  const token = await getAuthToken()
  const respuesta = await ApiClient.obtenerColecciones(0, 10, token)

  return (
    <div className="flex h-[calc(100vh-4rem)]">
      {/* Sidebar */}
      <div className="w-[420px] flex flex-col bg-content1 border-r border-divider">
        {/* Header */}
        <div className="p-6 border-b border-divider">
          <Link href="/administrador">
            <Button variant="light" size="sm" startContent={<ArrowLeft className="w-4 h-4" />} className="mb-4">
              Volver
            </Button>
          </Link>
          <div className="flex items-center justify-between mb-2">
            <h1 className="text-2xl font-bold text-foreground">Colecciones</h1>
            <Button color="primary" size="sm" startContent={<Plus className="w-4 h-4" />}>
              Nueva
            </Button>
          </div>
          <p className="text-sm text-default-500">{respuesta.totalElementos} colecciones</p>
        </div>

        {/* List */}
        <div className="flex-1 overflow-y-auto p-4">
          <div className="space-y-3">
            {respuesta.datos.map((coleccion) => (
              <Card key={coleccion.handle}>
                <CardBody className="p-4">
                  <div className="flex items-start gap-3">
                    <div className="w-10 h-10 flex items-center justify-center bg-primary/10 rounded-lg shrink-0">
                      <Folder className="w-5 h-5 text-primary" />
                    </div>
                    <div className="flex-1 min-w-0">
                      <h3 className="font-semibold text-base mb-1 text-foreground">{coleccion.titulo}</h3>
                      <p className="text-sm text-default-500 line-clamp-2 mb-3">{coleccion.descripcion}</p>

                      {coleccion.criteriosDePertenencia.length > 0 && (
                        <div className="flex flex-wrap gap-1.5 mb-3">
                          {coleccion.criteriosDePertenencia.map((criterio, idx) => (
                            <Chip key={idx} size="sm" variant="flat" color="secondary">
                              {criterio.categoria}
                            </Chip>
                          ))}
                        </div>
                      )}

                      <div className="flex gap-2 flex-wrap">
                        <Link href={`/administrador/colecciones/${coleccion.handle}`}>
                          <Button color="primary" size="sm" startContent={<Eye className="w-4 h-4" />}>
                            Ver
                          </Button>
                        </Link>
                        <Button variant="flat" size="sm" startContent={<Edit className="w-4 h-4" />}>
                          Editar
                        </Button>
                        <Button variant="flat" color="danger" size="sm" startContent={<Trash2 className="w-4 h-4" />}>
                          Eliminar
                        </Button>
                      </div>
                    </div>
                  </div>
                </CardBody>
              </Card>
            ))}
          </div>
        </div>
      </div>

      {/* Map */}
      <div className="flex-1">
        <MapWrapper />
      </div>
    </div>
  )
}

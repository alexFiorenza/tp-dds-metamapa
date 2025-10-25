import { ApiClient } from "@/lib/api-client"
import { getAuthToken } from "@/lib/auth-helpers"
import { MapWrapper } from "@/components/map-wrapper"
import { Button, Card, CardBody, Chip, ButtonGroup } from "@heroui/react"
import Link from "next/link"

export default async function AdminSolicitudesPage() {
  const token = await getAuthToken()
  const respuesta = await ApiClient.obtenerSolicitudes(0, 10, token)

  return (
    <div className="flex h-screen">
      {/* Sidebar */}
      <div className="w-[420px] flex flex-col bg-content1 border-r border-divider">
        {/* Header */}
        <div className="p-6 border-b border-divider">
          <Link href="/administrador">
            <Button variant="light" size="sm" startContent={<i className="ri-arrow-left-line w-4 h-4" />} className="mb-4">
              Volver
            </Button>
          </Link>
          <h1 className="text-2xl font-bold text-foreground">Solicitudes de Eliminación</h1>
          <p className="text-sm text-default-500 mt-2">{respuesta.totalElementos} solicitudes</p>
        </div>

        {/* Filters */}
        <div className="p-4 border-b border-divider">
          <ButtonGroup className="w-full" size="sm" variant="bordered">
            <Button className="flex-1">Todas</Button>
            <Button className="flex-1">Pendientes</Button>
            <Button className="flex-1">Resueltas</Button>
          </ButtonGroup>
        </div>

        {/* List */}
        <div className="flex-1 overflow-y-auto p-4">
          <div className="space-y-3">
            {respuesta.datos.map((solicitud) => (
              <Card key={solicitud.uuid}>
                <CardBody className="p-4">
                  <div className="space-y-3">
                    <div className="flex items-start justify-between gap-2">
                      <Chip
                        size="sm"
                        variant="flat"
                        color={
                          solicitud.estado === "PENDIENTE"
                            ? "warning"
                            : solicitud.estado === "ACEPTADA"
                              ? "success"
                              : "default"
                        }
                        startContent={solicitud.estado === "PENDIENTE" ? <i className="ri-time-line w-3 h-3" /> : null}
                      >
                        {solicitud.estado}
                      </Chip>
                      <span className="text-xs text-default-500">
                        {new Date(solicitud.fechaSolicitud).toLocaleDateString("es-AR", {
                          day: '2-digit',
                          month: 'short',
                          year: 'numeric'
                        })}
                      </span>
                    </div>

                    <div>
                      <p className="text-sm font-semibold mb-1 text-foreground">Hecho: {solicitud.hecho}</p>
                      <p className="text-sm text-default-600">{solicitud.texto}</p>
                    </div>

                    {solicitud.estado === "PENDIENTE" && (
                      <div className="flex gap-2">
                        <Button
                          color="success"
                          size="sm"
                          className="flex-1"
                          startContent={<i className="ri-check-line w-4 h-4" />}
                        >
                          Aceptar
                        </Button>
                        <Button
                          color="danger"
                          variant="flat"
                          size="sm"
                          className="flex-1"
                          startContent={<i className="ri-close-line w-4 h-4" />}
                        >
                          Rechazar
                        </Button>
                      </div>
                    )}
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

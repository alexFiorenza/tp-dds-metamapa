import { MapWrapper } from "@/components/map-wrapper"
import { Button, Card, CardBody } from "@heroui/react"
import Link from "next/link"

export default function AdminPage() {
  return (
    <div className="flex h-screen">
      {/* Sidebar */}
      <div className="w-96 flex flex-col bg-content1 border-r border-divider">
        {/* Header */}
        <div className="p-6 border-b border-divider">
          <Link href="/">
            <Button variant="light" size="sm" startContent={<i className="ri-arrow-left-line w-4 h-4" />} className="mb-4">
              Volver al inicio
            </Button>
          </Link>
          <h1 className="text-2xl font-bold text-foreground">Panel de Administrador</h1>
          <p className="text-sm text-default-500 mt-2">Gestión de colecciones y solicitudes</p>
        </div>

        {/* Menu */}
        <div className="flex-1 overflow-y-auto p-4">
          <div className="space-y-3">
            <Link href="/administrador/colecciones">
              <Card isPressable isHoverable>
                <CardBody className="p-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 flex items-center justify-center bg-primary/10 rounded-xl">
                      <i className="ri-folder-open-line w-6 h-6 text-primary" />
                    </div>
                    <div className="flex-1">
                      <h3 className="font-semibold text-base text-foreground">Gestionar Colecciones</h3>
                      <p className="text-sm text-default-500 mt-1">Crear, editar y eliminar colecciones</p>
                    </div>
                  </div>
                </CardBody>
              </Card>
            </Link>

            <Link href="/administrador/solicitudes">
              <Card isPressable isHoverable>
                <CardBody className="p-4">
                  <div className="flex items-center gap-4">
                    <div className="w-12 h-12 flex items-center justify-center bg-warning/10 rounded-xl">
                      <i className="ri-alert-line w-6 h-6 text-warning" />
                    </div>
                    <div className="flex-1">
                      <h3 className="font-semibold text-base text-foreground">Solicitudes de Eliminación</h3>
                      <p className="text-sm text-default-500 mt-1">Revisar y gestionar solicitudes</p>
                    </div>
                  </div>
                </CardBody>
              </Card>
            </Link>
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

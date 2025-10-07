import { SidebarLayout } from "@/components/sidebar-layout"
import { MapWrapper } from "@/components/map-wrapper"
import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { ArrowLeft, FolderOpen, AlertCircle, BarChart } from "lucide-react"
import Link from "next/link"

export default function AdminPage() {
  return (
    <SidebarLayout
      sidebar={
        <div className="flex flex-col h-full">
          {/* Header */}
          <div className="p-4 border-b border-border">
            <Link href="/">
              <Button variant="ghost" size="sm" className="mb-3">
                <ArrowLeft className="h-4 w-4 mr-2" />
                Volver al inicio
              </Button>
            </Link>
            <h1 className="text-2xl font-bold">Panel de Administrador</h1>
            <p className="text-sm text-muted-foreground mt-1">Gestión de colecciones y solicitudes</p>
          </div>

          {/* Menu */}
          <div className="flex-1 overflow-y-auto p-4">
            <div className="space-y-3">
              <Link href="/administrador/colecciones">
                <Card className="p-4 hover:bg-accent/50 transition-colors cursor-pointer">
                  <div className="flex items-center gap-3">
                    <div className="p-3 bg-blue-500/10 rounded-lg">
                      <FolderOpen className="h-6 w-6 text-blue-500" />
                    </div>
                    <div>
                      <h3 className="font-semibold">Gestionar Colecciones</h3>
                      <p className="text-sm text-muted-foreground">Crear, editar y eliminar colecciones</p>
                    </div>
                  </div>
                </Card>
              </Link>

              <Link href="/administrador/solicitudes">
                <Card className="p-4 hover:bg-accent/50 transition-colors cursor-pointer">
                  <div className="flex items-center gap-3">
                    <div className="p-3 bg-orange-500/10 rounded-lg">
                      <AlertCircle className="h-6 w-6 text-orange-500" />
                    </div>
                    <div>
                      <h3 className="font-semibold">Solicitudes de Eliminación</h3>
                      <p className="text-sm text-muted-foreground">Revisar y gestionar solicitudes</p>
                    </div>
                  </div>
                </Card>
              </Link>

              <Card className="p-4 bg-muted/30">
                <div className="flex items-center gap-3">
                  <div className="p-3 bg-green-500/10 rounded-lg">
                    <BarChart className="h-6 w-6 text-green-500" />
                  </div>
                  <div>
                    <h3 className="font-semibold">Estadísticas</h3>
                    <p className="text-sm text-muted-foreground">Próximamente</p>
                  </div>
                </div>
              </Card>
            </div>
          </div>
        </div>
      }
    >
      <MapWrapper />
    </SidebarLayout>
  )
}

"use client"

import { useState, useMemo } from "react"
import type { HechoDTO } from "@/types/api"
import {
  Modal,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Button,
  Chip,
  Divider,
  Textarea,
  Card,
  CardBody,
} from "@heroui/react"
import Image from "next/image"
import { useUser } from "@clerk/nextjs"
import { useRouter } from "next/navigation"
import { getCategoriaStyle } from "@/lib/categoria-styles"

interface HechoDetailModalProps {
  hecho: HechoDTO | null
  isOpen: boolean
  onClose: () => void
}

export function HechoDetailModal({ hecho, isOpen, onClose }: HechoDetailModalProps) {
  const [mostrarFormularioEliminacion, setMostrarFormularioEliminacion] = useState(false)
  const [motivo, setMotivo] = useState("")
  const { user, isLoaded } = useUser()
  const router = useRouter()

  const puedeEditar = useMemo(() => {
    // Esperar a que el usuario esté cargado
    if (!isLoaded) return false
    
    const contribuyenteId = hecho?.contribuyente?.userId
    
    if (!hecho || !user || !contribuyenteId) {
      // Debug: ver qué valores tenemos
      if (hecho) {
        console.log('Debug puedeEditar - condiciones no cumplidas:', {
          tieneHecho: !!hecho,
          tieneUser: !!user,
          tieneContribuyente: !!hecho.contribuyente,
          contribuyenteId: contribuyenteId,
          userId: user?.id,
          match: user?.id === contribuyenteId
        })
      }
      return false
    }
    
    // Verificar que el usuario es el contribuyente
    const esContribuyente = user.id === contribuyenteId
    
    // Verificar que ha pasado menos de una semana
    let dentroDelPlazo = true
    if (hecho.fechaCarga) {
      const fechaCarga = new Date(hecho.fechaCarga)
      const ahora = new Date()
      const diasTranscurridos = Math.floor((ahora.getTime() - fechaCarga.getTime()) / (1000 * 60 * 60 * 24))
      dentroDelPlazo = diasTranscurridos < 7
      
      console.log('Debug puedeEditar:', {
        userId: user.id,
        contribuyenteId: contribuyenteId,
        esContribuyente,
        fechaCarga: hecho.fechaCarga,
        diasTranscurridos,
        dentroDelPlazo,
        puedeEditar: esContribuyente && dentroDelPlazo
      })
    } else {
      console.log('Debug puedeEditar (sin fechaCarga):', {
        userId: user.id,
        contribuyenteId: contribuyenteId,
        esContribuyente,
        puedeEditar: esContribuyente
      })
    }
    
    return esContribuyente && dentroDelPlazo
  }, [user, hecho, isLoaded])

  if (!hecho) return null

  const { iconClass, color } = getCategoriaStyle(hecho.categoria)

  const handleEditar = () => {
    onClose()
    router.push(`/hechos/editar/${hecho.uuid}`)
  }

  // Detectar tipo de archivo por extensión
  const isImageUrl = (url: string) => /\.(jpg|jpeg|png|gif|webp)$/i.test(url)
  const isVideoUrl = (url: string) => /\.(mp4|mpeg|mov|webm)$/i.test(url)

  const imagenes = hecho.multimedia?.filter(isImageUrl) || []
  const videos = hecho.multimedia?.filter(isVideoUrl) || []

  const handleClose = () => {
    setMostrarFormularioEliminacion(false)
    setMotivo("")
    onClose()
  }

  const handleSubmitSolicitud = () => {
    // TODO: Implementar lógica de envío de solicitud
    console.log("Solicitud de eliminación:", { hechoId: hecho.uuid, motivo })
    alert(`Solicitud enviada para el hecho: ${hecho.titulo}\nMotivo: ${motivo}`)
    setMostrarFormularioEliminacion(false)
    setMotivo("")
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      size="3xl"
      scrollBehavior="inside"
      backdrop="opaque"
      placement="center"
      classNames={{
        backdrop: "bg-black/70 backdrop-blur-md",
        wrapper: "items-center px-4",
        base: "bg-content1 max-h-[90vh]",
      }}
    >
      <ModalContent className="bg-content1">
        {(onCloseModal) => (
          <>
            <ModalHeader className="flex flex-col gap-2 border-b border-divider px-6 py-4">
              <div className="flex items-start justify-between gap-4">
                <div className="flex items-start gap-3 flex-1">
                  <div className={`w-10 h-10 rounded-lg flex items-center justify-center bg-${color}/10 shrink-0`}>
                    <i className={`${iconClass} w-5 h-5 text-${color}`} />
                  </div>
                  <div className="flex-1">
                    <h2 className="text-xl font-bold text-foreground">{hecho.titulo}</h2>
                    <div className="flex items-center gap-2 mt-1">
                      <Chip size="sm" color={color} variant="flat">
                        {hecho.categoria}
                      </Chip>
                      <Chip size="sm" variant="flat">
                        {hecho.estado}
                      </Chip>
                    </div>
                  </div>
                </div>
              </div>
            </ModalHeader>

            <ModalBody className="px-6 py-6">
              <div className="space-y-6">
                {/* Galería de imágenes */}
                {imagenes.length > 0 && (
                  <div className="space-y-3">
                    <h3 className="text-sm font-semibold text-foreground flex items-center gap-2">
                      <i className="ri-image-line w-4 h-4" />
                      Imágenes ({imagenes.length})
                    </h3>
                    <div className="grid grid-cols-2 gap-3">
                      {imagenes.map((imagenUrl, idx) => (
                        <div key={idx} className="relative w-full h-40 bg-default-100 rounded-lg overflow-hidden">
                          <Image
                            src={imagenUrl}
                            alt={`${hecho.titulo} - imagen ${idx + 1}`}
                            fill
                            className="object-cover"
                            unoptimized
                          />
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Galería de videos */}
                {videos.length > 0 && (
                  <div className="space-y-3">
                    <h3 className="text-sm font-semibold text-foreground flex items-center gap-2">
                      <i className="ri-video-line w-4 h-4" />
                      Videos ({videos.length})
                    </h3>
                    <div className="grid grid-cols-2 gap-3">
                      {videos.map((videoUrl, idx) => (
                        <div key={idx} className="relative w-full h-40 bg-default-100 rounded-lg overflow-hidden">
                          <video
                            src={videoUrl}
                            controls
                            className="w-full h-full object-cover"
                            preload="metadata"
                          >
                            Tu navegador no soporta el elemento de video.
                          </video>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Descripción */}
                <div className="space-y-2">
                  <h3 className="text-sm font-semibold text-foreground flex items-center gap-2">
                    <i className="ri-file-text-line w-4 h-4" />
                    Descripción
                  </h3>
                  <p className="text-sm text-default-600 leading-relaxed">{hecho.descripcion}</p>
                </div>

                <Divider />

                {/* Información detallada */}
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-1">
                    <p className="text-xs text-default-500 flex items-center gap-1.5">
                      <i className="ri-calendar-line w-4 h-4" />
                      Fecha del acontecimiento
                    </p>
                    <p className="text-sm font-medium text-foreground">
                      {new Date(hecho.fechaAcontecimiento).toLocaleDateString("es-AR", {
                        day: "2-digit",
                        month: "long",
                        year: "numeric",
                      })}
                    </p>
                  </div>

                  <div className="space-y-1">
                    <p className="text-xs text-default-500 flex items-center gap-1.5">
                      <i className="ri-time-line w-4 h-4" />
                      Fecha de carga
                    </p>
                    <p className="text-sm font-medium text-foreground">
                      {new Date(hecho.fechaCarga).toLocaleDateString("es-AR", {
                        day: "2-digit",
                        month: "long",
                        year: "numeric",
                      })}
                    </p>
                  </div>

                  <div className="space-y-1">
                    <p className="text-xs text-default-500 flex items-center gap-1.5">
                      <i className="ri-map-pin-line w-4 h-4" />
                      Coordenadas
                    </p>
                    <p className="text-sm font-medium text-foreground font-mono">
                      {hecho.latitud.toFixed(6)}, {hecho.longitud.toFixed(6)}
                    </p>
                  </div>

                  <div className="space-y-1">
                    <p className="text-xs text-default-500 flex items-center gap-1.5">
                      <i className="ri-fingerprint-line w-4 h-4" />
                      UUID
                    </p>
                    <p className="text-xs font-medium text-foreground font-mono break-all">{hecho.uuid}</p>
                  </div>
                </div>

                <Divider />

                {/* Contribuyente */}
                {hecho.contribuyente?.nombre && (
                  <div className="space-y-2">
                    <p className="text-xs text-default-500 flex items-center gap-1.5">
                      <i className="ri-user-line w-4 h-4" />
                      Contribuyente
                    </p>
                    <p className="text-sm font-medium text-foreground">
                      {hecho.contribuyente.nombre}
                    </p>
                  </div>
                )}

                {/* Origen */}
                <div className="space-y-2">
                  <p className="text-xs text-default-500 flex items-center gap-1.5">
                    <i className="ri-link-line w-4 h-4" />
                    Origen
                  </p>
                  <p className="text-sm font-medium text-foreground">{hecho.origen}</p>
                </div>

                {/* Etiquetas */}
                {hecho.etiquetas.length > 0 && (
                  <div className="space-y-2">
                    <p className="text-xs text-default-500 flex items-center gap-1.5">
                      <i className="ri-price-tag-3-line w-4 h-4" />
                      Etiquetas
                    </p>
                    <div className="flex flex-wrap gap-2">
                      {hecho.etiquetas.map((etiqueta) => (
                        <Chip key={etiqueta} size="sm" variant="flat" color="secondary">
                          {etiqueta}
                        </Chip>
                      ))}
                    </div>
                  </div>
                )}

                {/* Formulario de solicitud de eliminación */}
                {mostrarFormularioEliminacion && (
                  <>
                    <Divider />
                    <Card className="bg-danger-50 border border-danger-200">
                      <CardBody className="p-4 space-y-4">
                        <div className="flex items-start gap-3">
                          <div className="w-10 h-10 flex items-center justify-center bg-danger/20 rounded-lg shrink-0">
                            <i className="ri-error-warning-line w-5 h-5 text-danger" />
                          </div>
                          <div className="flex-1">
                            <h4 className="font-semibold text-danger-700 mb-1">Solicitar eliminación</h4>
                            <p className="text-sm text-danger-600 mb-3">
                              Esta solicitud será revisada por un administrador antes de procesar la eliminación del
                              hecho.
                            </p>

                            <Textarea
                              label="Motivo de la solicitud"
                              placeholder="Explique por qué este hecho debería ser eliminado..."
                              value={motivo}
                              onChange={(e) => setMotivo(e.target.value)}
                              variant="bordered"
                              minRows={3}
                              maxRows={6}
                              classNames={{
                                input: "bg-white",
                              }}
                            />

                            <div className="flex gap-2 mt-4">
                              <Button
                                color="danger"
                                variant="flat"
                                size="sm"
                                onPress={() => {
                                  setMostrarFormularioEliminacion(false)
                                  setMotivo("")
                                }}
                              >
                                Cancelar
                              </Button>
                              <Button
                                color="danger"
                                size="sm"
                                onPress={handleSubmitSolicitud}
                                isDisabled={!motivo.trim()}
                                startContent={<i className="ri-send-plane-line" />}
                              >
                                Enviar solicitud
                              </Button>
                            </div>
                          </div>
                        </div>
                      </CardBody>
                    </Card>
                  </>
                )}
              </div>
            </ModalBody>

            <ModalFooter className="border-t border-divider px-6 py-4">
              <Button variant="light" onPress={handleClose}>
                Cerrar
              </Button>
              {!mostrarFormularioEliminacion && (
                <>
                  {puedeEditar && (
                    <Button
                      color="primary"
                      variant="flat"
                      startContent={<i className="ri-edit-line" />}
                      onPress={handleEditar}
                    >
                      Editar
                    </Button>
                  )}
                  <Button
                    color="danger"
                    variant="flat"
                    startContent={<i className="ri-delete-bin-line" />}
                    onPress={() => setMostrarFormularioEliminacion(true)}
                  >
                    Solicitar eliminación
                  </Button>
                </>
              )}
            </ModalFooter>
          </>
        )}
      </ModalContent>
    </Modal>
  )
}

"use client"

import { useState, useEffect } from "react"
import {
  Modal,
  ModalContent,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Button,
  Input,
  Textarea,
  Select,
  SelectItem,
  Chip,
  Card,
  CardBody,
} from "@heroui/react"
import type { ColeccionCreateDTO, CriterioCreateDTO, FuenteDTO } from "@/types/api"

interface FormularioColeccionProps {
  isOpen: boolean
  onClose: () => void
  onSubmit: (coleccion: ColeccionCreateDTO) => Promise<void>
  fuentes: FuenteDTO[]
  coleccionInicial?: ColeccionCreateDTO
  titulo?: string
}

const TIPOS_CRITERIO = [
  { value: "titulo", label: "Título" },
  { value: "categoria", label: "Categoría" },
  { value: "descripcion", label: "Descripción" },
  { value: "origen", label: "Origen" },
  { value: "fechaAcontecimiento", label: "Fecha de Acontecimiento" },
  { value: "fechaCarga", label: "Fecha de Carga" },
  { value: "estado", label: "Estado" },
  { value: "etiquetas", label: "Etiquetas" },
  { value: "latitud", label: "Latitud" },
  { value: "longitud", label: "Longitud" },
]

const ESTADOS = ["ACTIVO", "OCULTO"]

export function FormularioColeccion({
  isOpen,
  onClose,
  onSubmit,
  fuentes,
  coleccionInicial,
  titulo = "Nueva Colección",
}: FormularioColeccionProps) {
  const [formData, setFormData] = useState<ColeccionCreateDTO>({
    titulo: "",
    descripcion: "",
    fuentesIds: [],
    criteriosDePertenencia: [],
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  // Nuevo criterio temporal
  const [nuevoCriterio, setNuevoCriterio] = useState<Partial<CriterioCreateDTO>>({
    tipo: "categoria",
  })

  useEffect(() => {
    if (coleccionInicial) {
      setFormData(coleccionInicial)
    } else {
      setFormData({
        titulo: "",
        descripcion: "",
        fuentesIds: [],
        criteriosDePertenencia: [],
      })
    }
  }, [coleccionInicial, isOpen])

  const handleSubmit = async () => {
    try {
      setError(null)

      // Validaciones
      if (!formData.titulo.trim()) {
        setError("El título es requerido")
        return
      }
      if (!formData.descripcion.trim()) {
        setError("La descripción es requerida")
        return
      }
      if (formData.fuentesIds.length === 0) {
        setError("Debe seleccionar al menos una fuente")
        return
      }

      setLoading(true)
      await onSubmit(formData)
      handleClose()
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error al guardar colección")
    } finally {
      setLoading(false)
    }
  }

  const handleClose = () => {
    setFormData({
      titulo: "",
      descripcion: "",
      fuentesIds: [],
      criteriosDePertenencia: [],
    })
    setNuevoCriterio({ tipo: "categoria" })
    setError(null)
    onClose()
  }

  const toggleFuente = (uuid: string) => {
    setFormData((prev) => ({
      ...prev,
      fuentesIds: prev.fuentesIds.includes(uuid)
        ? prev.fuentesIds.filter((id) => id !== uuid)
        : [...prev.fuentesIds, uuid],
    }))
  }

  const agregarCriterio = () => {
    if (!nuevoCriterio.tipo) return

    const criterio: CriterioCreateDTO = {
      tipo: nuevoCriterio.tipo,
      ...(nuevoCriterio.titulo && { titulo: nuevoCriterio.titulo }),
      ...(nuevoCriterio.categoria && { categoria: nuevoCriterio.categoria }),
      ...(nuevoCriterio.descripcion && { descripcion: nuevoCriterio.descripcion }),
      ...(nuevoCriterio.origen && { origen: nuevoCriterio.origen }),
      ...(nuevoCriterio.fechaAcontecimiento && { fechaAcontecimiento: nuevoCriterio.fechaAcontecimiento }),
      ...(nuevoCriterio.fechaCarga && { fechaCarga: nuevoCriterio.fechaCarga }),
      ...(nuevoCriterio.estado && { estado: nuevoCriterio.estado }),
      ...(nuevoCriterio.etiquetas && { etiquetas: nuevoCriterio.etiquetas }),
      ...(nuevoCriterio.latitud !== undefined && { latitud: nuevoCriterio.latitud }),
      ...(nuevoCriterio.longitud !== undefined && { longitud: nuevoCriterio.longitud }),
    }

    setFormData((prev) => ({
      ...prev,
      criteriosDePertenencia: [...prev.criteriosDePertenencia, criterio],
    }))

    setNuevoCriterio({ tipo: "categoria" })
  }

  const eliminarCriterio = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      criteriosDePertenencia: prev.criteriosDePertenencia.filter((_, i) => i !== index),
    }))
  }

  const renderCampoValor = () => {
    const commonClasses = {
      label: "text-sm font-medium mb-1.5"
    }

    switch (nuevoCriterio.tipo) {
      case "titulo":
      case "descripcion":
      case "origen":
      case "categoria":
        return (
          <Input
            label={nuevoCriterio.tipo.charAt(0).toUpperCase() + nuevoCriterio.tipo.slice(1)}
            placeholder={`Ingrese ${nuevoCriterio.tipo}`}
            value={nuevoCriterio[nuevoCriterio.tipo as keyof CriterioCreateDTO] as string || ""}
            onChange={(e) => setNuevoCriterio({ ...nuevoCriterio, [nuevoCriterio.tipo]: e.target.value })}
            size="sm"
            variant="bordered"
            labelPlacement="outside"
            classNames={commonClasses}
          />
        )
      case "estado":
        return (
          <Select
            label="Estado"
            placeholder="Seleccione estado"
            selectedKeys={nuevoCriterio.estado ? [nuevoCriterio.estado] : []}
            onSelectionChange={(keys) => {
              const value = Array.from(keys)[0] as string
              setNuevoCriterio({ ...nuevoCriterio, estado: value })
            }}
            size="sm"
            variant="bordered"
            labelPlacement="outside"
            classNames={commonClasses}
          >
            {ESTADOS.map((est) => (
              <SelectItem key={est} value={est}>
                {est}
              </SelectItem>
            ))}
          </Select>
        )
      case "fechaAcontecimiento":
        return (
          <Input
            type="date"
            label="Fecha de Acontecimiento"
            value={nuevoCriterio.fechaAcontecimiento || ""}
            onChange={(e) => setNuevoCriterio({ ...nuevoCriterio, fechaAcontecimiento: e.target.value })}
            size="sm"
            variant="bordered"
            labelPlacement="outside"
            classNames={commonClasses}
          />
        )
      case "fechaCarga":
        return (
          <Input
            type="datetime-local"
            label="Fecha de Carga"
            value={nuevoCriterio.fechaCarga || ""}
            onChange={(e) => setNuevoCriterio({ ...nuevoCriterio, fechaCarga: e.target.value })}
            size="sm"
            variant="bordered"
            labelPlacement="outside"
            classNames={commonClasses}
          />
        )
      case "latitud":
      case "longitud":
        return (
          <Input
            type="number"
            label={nuevoCriterio.tipo === "latitud" ? "Latitud" : "Longitud"}
            placeholder="Ej: -34.6037"
            value={String(nuevoCriterio[nuevoCriterio.tipo as keyof CriterioCreateDTO] || "")}
            onChange={(e) =>
              setNuevoCriterio({ ...nuevoCriterio, [nuevoCriterio.tipo]: e.target.value ? parseFloat(e.target.value) : undefined })
            }
            size="sm"
            variant="bordered"
            labelPlacement="outside"
            classNames={commonClasses}
          />
        )
      case "etiquetas":
        return (
          <Input
            label="Etiquetas"
            placeholder="Ej: deportes,futbol,estadio"
            value={nuevoCriterio.etiquetas || ""}
            onChange={(e) => setNuevoCriterio({ ...nuevoCriterio, etiquetas: e.target.value })}
            size="sm"
            variant="bordered"
            labelPlacement="outside"
            classNames={commonClasses}
          />
        )
      default:
        return null
    }
  }

  const renderCriterio = (criterio: CriterioCreateDTO, index: number) => {
    const valor = criterio[criterio.tipo as keyof CriterioCreateDTO]
    return (
      <Chip
        key={index}
        onClose={() => eliminarCriterio(index)}
        variant="flat"
        color="secondary"
      >
        {criterio.tipo}: {String(valor)}
      </Chip>
    )
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
        wrapper: "items-center",
        base: "bg-content1 max-h-[90vh]",
      }}
    >
      <ModalContent className="bg-content1">
        <ModalHeader className="flex flex-col gap-1 border-b border-divider">{titulo}</ModalHeader>
        <ModalBody className="py-6">
          <div className="space-y-6">
            {error && (
              <div className="p-3 bg-danger-50 border border-danger-200 rounded-lg text-danger-700 text-sm">
                {error}
              </div>
            )}

            {/* Información básica */}
            <div className="space-y-4">
              <h3 className="text-sm font-semibold text-foreground">Información Básica</h3>
              <div className="space-y-4">
                <Input
                  label="Título"
                  placeholder="Ej: Eventos Deportivos Buenos Aires"
                  value={formData.titulo}
                  onChange={(e) => setFormData({ ...formData, titulo: e.target.value })}
                  isRequired
                  variant="bordered"
                  labelPlacement="outside"
                  classNames={{
                    label: "text-sm font-medium mb-1.5"
                  }}
                />
                <Textarea
                  label="Descripción"
                  placeholder="Ej: Colección de eventos deportivos que ocurrieron en Buenos Aires"
                  value={formData.descripcion}
                  onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                  isRequired
                  variant="bordered"
                  labelPlacement="outside"
                  minRows={3}
                  classNames={{
                    label: "text-sm font-medium mb-1.5"
                  }}
                />
              </div>
            </div>

            {/* Fuentes */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-foreground">Fuentes de Datos</h3>
                <Chip size="sm" variant="flat" color="primary">
                  {formData.fuentesIds.length} seleccionadas
                </Chip>
              </div>
              {fuentes.length === 0 ? (
                <div className="p-4 bg-default-100 rounded-lg text-center text-sm text-default-500">
                  No hay fuentes disponibles
                </div>
              ) : (
                <div className="max-h-60 overflow-y-auto space-y-2 p-3 bg-default-50 rounded-lg border border-divider">
                  {fuentes.map((fuente) => (
                    <Card
                      key={fuente.uuid}
                      isPressable
                      onPress={() => toggleFuente(fuente.uuid)}
                      shadow="sm"
                      className={
                        formData.fuentesIds.includes(fuente.uuid)
                          ? "border-2 border-primary bg-primary-50"
                          : "border border-divider bg-content1"
                      }
                    >
                      <CardBody className="p-3">
                        <div className="flex items-center gap-3">
                          <div
                            className={`w-5 h-5 rounded border-2 flex items-center justify-center shrink-0 ${
                              formData.fuentesIds.includes(fuente.uuid)
                                ? "bg-primary border-primary"
                                : "border-default-300 bg-white"
                            }`}
                          >
                            {formData.fuentesIds.includes(fuente.uuid) && (
                              <i className="ri-check-line text-white text-xs" />
                            )}
                          </div>
                          <div className="flex-1 min-w-0">
                            <p className="text-sm font-medium text-foreground truncate">{fuente.host}</p>
                            <p className="text-xs text-default-500 truncate">UUID: {fuente.uuid}</p>
                          </div>
                        </div>
                      </CardBody>
                    </Card>
                  ))}
                </div>
              )}
            </div>

            {/* Criterios de Pertenencia */}
            <div className="space-y-3">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-semibold text-foreground">Criterios de Pertenencia</h3>
                <p className="text-xs text-default-500">Filtros para incluir hechos automáticamente</p>
              </div>

              {/* Criterios existentes */}
              {formData.criteriosDePertenencia.length > 0 && (
                <div className="flex flex-wrap gap-2 p-3 bg-default-50 rounded-lg border border-divider">
                  {formData.criteriosDePertenencia.map((criterio, index) =>
                    renderCriterio(criterio, index)
                  )}
                </div>
              )}

              {/* Agregar nuevo criterio */}
              <Card shadow="sm" className="border border-divider">
                <CardBody className="space-y-4 p-4">
                  <p className="text-xs font-medium text-default-600">Agregar Criterio</p>
                  <Select
                    label="Tipo de Criterio"
                    placeholder="Seleccione el tipo"
                    selectedKeys={nuevoCriterio.tipo ? [nuevoCriterio.tipo] : []}
                    onSelectionChange={(keys) => {
                      const value = Array.from(keys)[0] as string
                      setNuevoCriterio({ tipo: value })
                    }}
                    size="sm"
                    variant="bordered"
                    labelPlacement="outside"
                    classNames={{
                      label: "text-sm font-medium mb-1.5"
                    }}
                  >
                    {TIPOS_CRITERIO.map((tipo) => (
                      <SelectItem key={tipo.value} value={tipo.value}>
                        {tipo.label}
                      </SelectItem>
                    ))}
                  </Select>

                  {renderCampoValor()}

                  <Button
                    color="primary"
                    variant="flat"
                    size="sm"
                    onPress={agregarCriterio}
                    startContent={<i className="ri-add-line" />}
                    className="w-full"
                  >
                    Agregar Criterio
                  </Button>
                </CardBody>
              </Card>
            </div>
          </div>
        </ModalBody>
        <ModalFooter className="gap-2 border-t border-divider">
          <Button variant="light" onPress={handleClose} isDisabled={loading}>
            Cancelar
          </Button>
          <Button
            color="primary"
            onPress={handleSubmit}
            isLoading={loading}
            startContent={!loading ? <i className="ri-save-line" /> : undefined}
          >
            {loading ? "Guardando..." : "Guardar Colección"}
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  )
}

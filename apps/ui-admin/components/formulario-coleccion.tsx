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
  Chip,
  Card,
  CardBody,
  Select,
  SelectItem,
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

const ESTADOS = ["ACTIVO", "OCULTO"]

const ALGORITMOS_CONSENSO = [
  { value: "default", label: "Por Defecto" },
  { value: "menciones", label: "Menciones" },
  { value: "simple", label: "Simple" },
  { value: "absoluta", label: "Absoluta" },
]

const TIPOS_CRITERIO = [
  { value: "TITULO", label: "Título" },
  { value: "CATEGORIA", label: "Categoría" },
  { value: "DESCRIPCION", label: "Descripción" },
  { value: "ORIGEN", label: "Origen" },
  { value: "FECHA_ACONTECIMIENTO", label: "Fecha de Acontecimiento" },
  { value: "ESTADO", label: "Estado" },
  { value: "ETIQUETAS", label: "Etiquetas" },
  { value: "COORDENADAS", label: "Coordenadas" },
]

// Obtener categorías desde variable de entorno
const CATEGORIAS = (process.env.NEXT_PUBLIC_CATEGORIAS || "INCENDIO,CONTAMINACION,MANIFESTACION,INUNDACION,FAUNA,ALUD,OTRO").split(",")

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
    algoritmoConsenso: "default",
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [nuevoCriterio, setNuevoCriterio] = useState<CriterioCreateDTO>({
    tipo: "",
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
        algoritmoConsenso: "default",
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
      algoritmoConsenso: "default",
    })
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
    if (!nuevoCriterio.tipo) {
      setError("Debe seleccionar un tipo de criterio")
      return
    }

    setFormData((prev) => ({
      ...prev,
      criteriosDePertenencia: [...prev.criteriosDePertenencia, { ...nuevoCriterio }],
    }))

    setNuevoCriterio({ tipo: "" })
    setError(null)
  }

  const eliminarCriterio = (index: number) => {
    setFormData((prev) => ({
      ...prev,
      criteriosDePertenencia: prev.criteriosDePertenencia.filter((_, i) => i !== index),
    }))
  }

  const actualizarCriterioValor = (campo: keyof CriterioCreateDTO, valor: string) => {
    setNuevoCriterio((prev) => ({
      ...prev,
      [campo]: valor,
    }))
  }

  // Función para validar si el nuevo criterio está completo
  const isNuevoCriterioValido = () => {
    if (!nuevoCriterio.tipo) return false

    const tipo = nuevoCriterio.tipo.toLowerCase()

    // Validar que el campo correspondiente al tipo tenga valor
    switch (tipo) {
      case "titulo":
        return !!nuevoCriterio.titulo?.trim()
      case "categoria":
        return !!nuevoCriterio.categoria?.trim()
      case "descripcion":
        return !!nuevoCriterio.descripcion?.trim()
      case "origen":
        return !!nuevoCriterio.origen?.trim()
      case "fecha_acontecimiento":
        return !!nuevoCriterio.fechaAcontecimiento
      case "estado":
        return !!nuevoCriterio.estado?.trim()
      case "etiquetas":
        return !!nuevoCriterio.etiquetas?.trim()
      case "coordenadas":
        return !!(nuevoCriterio.longitud && nuevoCriterio.latitud)
      default:
        return false
    }
  }

  // Función para validar si el formulario está completo
  const isFormValid = () => {
    return (
      formData.titulo.trim() !== "" &&
      formData.descripcion.trim() !== "" &&
      formData.fuentesIds.length > 0
    )
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={handleClose}
      size="2xl"
      scrollBehavior="inside"
      backdrop="opaque"
      placement="center"
      classNames={{
        backdrop: "bg-black/70 backdrop-blur-md",
        wrapper: "items-center px-4",
        base: "bg-content1 max-h-[90vh] mx-4",
      }}
    >
      <ModalContent className="bg-content1">
        <ModalHeader className="flex flex-col gap-1 border-b border-divider px-8 py-6">{titulo}</ModalHeader>
        <ModalBody className="py-8 px-8">
          <div className="space-y-8">
            {error && (
              <div className="p-4 bg-danger-50 border border-danger-200 rounded-lg text-danger-700 text-sm">
                {error}
              </div>
            )}

            {/* Información básica */}
            <div className="space-y-6">
              <div className="border-b border-divider pb-2">
                <h3 className="text-lg font-semibold text-foreground">Información Básica</h3>
                <p className="text-sm text-default-500 mt-1">Datos principales de la colección</p>
              </div>
              <div className="space-y-6">
                <div className="space-y-3">
                  <label className="text-sm font-medium text-foreground">Título *</label>
                  <Input
                    placeholder="Ej: Eventos Deportivos Buenos Aires"
                    value={formData.titulo}
                    onChange={(e) => setFormData({ ...formData, titulo: e.target.value })}
                    variant="bordered"
                    size="md"
                  />
                </div>
                <div className="space-y-3">
                  <label className="text-sm font-medium text-foreground">Descripción *</label>
                  <Textarea
                    placeholder="Ej: Colección de eventos deportivos que ocurrieron en Buenos Aires"
                    value={formData.descripcion}
                    onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
                    variant="bordered"
                    minRows={3}
                    size="md"
                  />
                </div>
                <div className="space-y-3">
                  <label className="text-sm font-medium text-foreground">Algoritmo de Consenso</label>
                  <Select
                    placeholder="Seleccione un algoritmo de consenso"
                    selectedKeys={formData.algoritmoConsenso ? [formData.algoritmoConsenso] : ["default"]}
                    onSelectionChange={(keys) => {
                      const selectedKey = Array.from(keys)[0] as string
                      setFormData({ ...formData, algoritmoConsenso: selectedKey })
                    }}
                    variant="bordered"
                    size="md"
                    classNames={{
                      trigger: "bg-content1 data-[hover=true]:bg-content2 pr-8",
                      listboxWrapper: "max-h-[400px]",
                      popoverContent: "bg-content1",
                    }}
                    items={ALGORITMOS_CONSENSO.map(alg => ({ key: alg.value, label: alg.label }))}
                  >
                    {(item) => <SelectItem key={item.key}>{item.label}</SelectItem>}
                  </Select>
                </div>
              </div>
            </div>

            {/* Fuentes */}
            <div className="space-y-4">
              <div className="border-b border-divider pb-2">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-foreground">Fuentes de Datos</h3>
                    <p className="text-sm text-default-500 mt-1">Seleccione las fuentes de datos para esta colección</p>
                  </div>
                  <Chip size="sm" variant="flat" color="primary">
                    {formData.fuentesIds.length} seleccionadas
                  </Chip>
                </div>
              </div>
              {!Array.isArray(fuentes) || fuentes.length === 0 ? (
                <div className="p-6 bg-default-100 rounded-lg text-center text-sm text-default-500">
                  No hay fuentes disponibles
                </div>
              ) : (
                <div className="max-h-60 overflow-y-auto space-y-3 p-4 bg-default-50 rounded-lg border border-divider">
                  {fuentes.map((fuente) => (
                    <Card
                      key={fuente.uuid}
                      isPressable
                      onPress={() => toggleFuente(fuente.uuid)}
                      shadow="sm"
                      className={`w-full ${
                        formData.fuentesIds.includes(fuente.uuid)
                          ? "border-2 border-primary bg-primary-50"
                          : "border border-divider bg-content1"
                      }`}
                    >
                      <CardBody className="p-4">
                        <div className="flex items-center gap-4">
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
                            <p className="text-sm font-medium text-foreground truncate">
                              {fuente.tipo || 'Fuente sin tipo'}
                            </p>
                            <p className="text-xs text-default-500 truncate mt-1">
                              {fuente.params && Object.keys(fuente.params).length > 0
                                ? Object.entries(fuente.params)
                                    .map(([key, value]) => {
                                      // Convertir objetos y arrays a JSON string
                                      const displayValue = typeof value === 'object' && value !== null
                                        ? JSON.stringify(value)
                                        : String(value);
                                      return `${key}: ${displayValue}`;
                                    })
                                    .join(', ')
                                : 'Sin parámetros'}
                            </p>
                          </div>
                        </div>
                      </CardBody>
                    </Card>
                  ))}
                </div>
              )}
            </div>

            {/* Criterios de Pertenencia */}
            <div className="space-y-4">
              <div className="border-b border-divider pb-2">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="text-lg font-semibold text-foreground">Criterios de Pertenencia</h3>
                    <p className="text-sm text-default-500 mt-1">Defina los criterios para filtrar hechos</p>
                  </div>
                  <Chip size="sm" variant="flat" color="secondary">
                    {formData.criteriosDePertenencia.length} criterios
                  </Chip>
                </div>
              </div>

              {/* Formulario para agregar nuevo criterio */}
              <Card className="bg-default-50 border border-divider">
                <CardBody className="p-4 space-y-4">
                  <div className="space-y-3">
                    <label className="text-sm font-medium text-foreground">Tipo de Criterio</label>
                    <Select
                      placeholder="Seleccione un tipo de criterio"
                      selectedKeys={nuevoCriterio.tipo ? [nuevoCriterio.tipo] : []}
                      onSelectionChange={(keys) => {
                        const selectedKey = Array.from(keys)[0] as string
                        setNuevoCriterio({ tipo: selectedKey })
                      }}
                      variant="bordered"
                      size="md"
                      classNames={{
                        trigger: "bg-content1 data-[hover=true]:bg-content2 pr-8",
                        listboxWrapper: "max-h-[400px]",
                        popoverContent: "bg-content1",
                      }}
                      items={TIPOS_CRITERIO.map(tipo => ({ key: tipo.value, label: tipo.label }))}
                    >
                      {(item) => <SelectItem key={item.key}>{item.label}</SelectItem>}
                    </Select>
                  </div>

                  {/* Campos dinámicos según el tipo seleccionado */}
                  {nuevoCriterio.tipo === "TITULO" && (
                    <div className="space-y-3">
                      <label className="text-sm font-medium text-foreground">Título</label>
                      <Input
                        placeholder="Ej: Incendio"
                        value={nuevoCriterio.titulo || ""}
                        onChange={(e) => actualizarCriterioValor("titulo", e.target.value)}
                        variant="bordered"
                        size="md"
                      />
                    </div>
                  )}

                  {nuevoCriterio.tipo === "CATEGORIA" && (
                    <div className="space-y-3">
                      <label className="text-sm font-medium text-foreground">Categoría</label>
                      <Select
                        placeholder="Seleccione una categoría"
                        selectedKeys={nuevoCriterio.categoria ? [nuevoCriterio.categoria] : []}
                        onSelectionChange={(keys) => {
                          const selectedKey = Array.from(keys)[0] as string
                          actualizarCriterioValor("categoria", selectedKey)
                        }}
                        variant="bordered"
                        size="md"
                        classNames={{
                          trigger: "bg-content1 data-[hover=true]:bg-content2 pr-8",
                          listboxWrapper: "max-h-[400px]",
                          popoverContent: "bg-content1",
                        }}
                        items={CATEGORIAS.map(cat => ({ key: cat, label: cat }))}
                      >
                        {(item) => <SelectItem key={item.key}>{item.label}</SelectItem>}
                      </Select>
                    </div>
                  )}

                  {nuevoCriterio.tipo === "DESCRIPCION" && (
                    <div className="space-y-3">
                      <label className="text-sm font-medium text-foreground">Descripción</label>
                      <Textarea
                        placeholder="Ej: Eventos relacionados con..."
                        value={nuevoCriterio.descripcion || ""}
                        onChange={(e) => actualizarCriterioValor("descripcion", e.target.value)}
                        variant="bordered"
                        minRows={2}
                        size="md"
                      />
                    </div>
                  )}

                  {nuevoCriterio.tipo === "ORIGEN" && (
                    <div className="space-y-3">
                      <label className="text-sm font-medium text-foreground">Origen</label>
                      <Input
                        placeholder="Ej: Sistema de monitoreo"
                        value={nuevoCriterio.origen || ""}
                        onChange={(e) => actualizarCriterioValor("origen", e.target.value)}
                        variant="bordered"
                        size="md"
                      />
                    </div>
                  )}

                  {nuevoCriterio.tipo === "ESTADO" && (
                    <div className="space-y-3">
                      <label className="text-sm font-medium text-foreground">Estado</label>
                      <Select
                        placeholder="Seleccione un estado"
                        selectedKeys={nuevoCriterio.estado ? [nuevoCriterio.estado] : []}
                        onSelectionChange={(keys) => {
                          const selectedKey = Array.from(keys)[0] as string
                          actualizarCriterioValor("estado", selectedKey)
                        }}
                        variant="bordered"
                        size="md"
                        classNames={{
                          trigger: "bg-content1 data-[hover=true]:bg-content2 pr-8",
                          listboxWrapper: "max-h-[400px]",
                          popoverContent: "bg-content1",
                        }}
                        items={ESTADOS.map(estado => ({ key: estado, label: estado }))}
                      >
                        {(item) => <SelectItem key={item.key}>{item.label}</SelectItem>}
                      </Select>
                    </div>
                  )}

                  {nuevoCriterio.tipo === "ETIQUETAS" && (
                    <div className="space-y-3">
                      <label className="text-sm font-medium text-foreground">Etiquetas (separadas por coma)</label>
                      <Input
                        placeholder="Ej: urgente, critico"
                        value={nuevoCriterio.etiquetas || ""}
                        onChange={(e) => actualizarCriterioValor("etiquetas", e.target.value)}
                        variant="bordered"
                        size="md"
                      />
                    </div>
                  )}

                  {nuevoCriterio.tipo === "FECHA_ACONTECIMIENTO" && (
                    <div className="space-y-3">
                      <label className="text-sm font-medium text-foreground">Fecha de Acontecimiento</label>
                      <Input
                        type="date"
                        value={nuevoCriterio.fechaAcontecimiento || ""}
                        onChange={(e) => actualizarCriterioValor("fechaAcontecimiento", e.target.value)}
                        variant="bordered"
                        size="md"
                      />
                    </div>
                  )}

                  {nuevoCriterio.tipo === "COORDENADAS" && (
                    <div className="grid grid-cols-2 gap-4">
                      <div className="space-y-3">
                        <label className="text-sm font-medium text-foreground">Latitud</label>
                        <Input
                          type="number"
                          step="any"
                          placeholder="-34.6037"
                          value={nuevoCriterio.latitud?.toString() || ""}
                          onChange={(e) => actualizarCriterioValor("latitud", e.target.value)}
                          variant="bordered"
                          size="md"
                        />
                      </div>
                      <div className="space-y-3">
                        <label className="text-sm font-medium text-foreground">Longitud</label>
                        <Input
                          type="number"
                          step="any"
                          placeholder="-58.3816"
                          value={nuevoCriterio.longitud?.toString() || ""}
                          onChange={(e) => actualizarCriterioValor("longitud", e.target.value)}
                          variant="bordered"
                          size="md"
                        />
                      </div>
                    </div>
                  )}

                  {nuevoCriterio.tipo && (
                    <Button
                      color="secondary"
                      variant="flat"
                      onPress={agregarCriterio}
                      startContent={<i className="ri-add-line" />}
                      className="w-full"
                      isDisabled={!isNuevoCriterioValido()}
                    >
                      Agregar Criterio
                    </Button>
                  )}
                </CardBody>
              </Card>

              {/* Lista de criterios agregados */}
              {formData.criteriosDePertenencia.length > 0 && (
                <div className="space-y-3">
                  <p className="text-sm font-medium text-foreground">Criterios agregados:</p>
                  <div className="space-y-2">
                    {formData.criteriosDePertenencia.map((criterio, index) => (
                      <Card key={index} className="border border-divider bg-content1">
                        <CardBody className="p-3">
                          <div className="flex items-center justify-between">
                            <div className="flex-1">
                              <div className="flex items-center gap-2">
                                <Chip size="sm" color="secondary" variant="flat">
                                  {TIPOS_CRITERIO.find((t) => t.value === criterio.tipo)?.label}
                                </Chip>
                                <span className="text-sm text-foreground">
                                  {(() => {
                                    const tipo = criterio.tipo?.toLowerCase();
                                    if (tipo === 'titulo') return criterio.titulo;
                                    if (tipo === 'categoria') return criterio.categoria;
                                    if (tipo === 'descripcion') return criterio.descripcion;
                                    if (tipo === 'origen') return criterio.origen;
                                    if (tipo === 'estado') return criterio.estado;
                                    if (tipo === 'etiquetas') return criterio.etiquetas;
                                    if (tipo === 'fecha_acontecimiento') return criterio.fechaAcontecimiento;
                                    if (tipo === 'coordenadas' && criterio.latitud && criterio.longitud)
                                      return `(${criterio.latitud}, ${criterio.longitud})`;
                                    return "Sin valor";
                                  })()}
                                </span>
                              </div>
                            </div>
                            <Button
                              isIconOnly
                              size="sm"
                              variant="light"
                              color="danger"
                              onPress={() => eliminarCriterio(index)}
                            >
                              <i className="ri-delete-bin-line" />
                            </Button>
                          </div>
                        </CardBody>
                      </Card>
                    ))}
                  </div>
                </div>
              )}
            </div>

          </div>
        </ModalBody>
        <ModalFooter className="gap-2 border-t border-divider px-8 py-6">
          <Button variant="light" onPress={handleClose} isDisabled={loading}>
            Cancelar
          </Button>
          <Button
            color="primary"
            variant="solid"
            onPress={handleSubmit}
            isLoading={loading}
            isDisabled={!isFormValid() || loading}
            startContent={!loading ? <i className="ri-save-line" /> : undefined}
            className={!isFormValid() ? "opacity-50" : ""}
          >
            {loading ? "Guardando..." : "Guardar Colección"}
          </Button>
        </ModalFooter>
      </ModalContent>
    </Modal>
  )
}

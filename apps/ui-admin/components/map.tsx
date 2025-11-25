'use client'

import { useState, useCallback, useMemo, useEffect, useRef } from 'react'
import Map, {
  Marker,
  NavigationControl,
  GeolocateControl,
  FullscreenControl,
  ScaleControl,
  Popup,
  MapProps as ReactMapGLProps
} from 'react-map-gl/mapbox'
import 'mapbox-gl/dist/mapbox-gl.css'
import { motion, AnimatePresence } from 'motion/react'
import { useMapResize } from '@/hooks/use-map-resize'
import type { HechoDTO } from '@/types/api'

interface MapComponentProps extends Partial<ReactMapGLProps> {
  /**
   * Array de hechos para mostrar como marcadores en el mapa
   */
  hechos?: HechoDTO[]
  /**
   * Initial longitude for map center
   * @default -58.3816 (Buenos Aires)
   */
  initialLongitude?: number
  /**
   * Initial latitude for map center
   * @default -34.6037 (Buenos Aires)
   */
  initialLatitude?: number
  /**
   * Initial zoom level
   * @default 10
   */
  initialZoom?: number
  /**
   * Map container height
   * @default '100%'
   */
  height?: string | number
  /**
   * Map container width
   * @default '100%'
   */
  width?: string | number
  /**
   * Enable navigation controls
   * @default true
   */
  showControls?: boolean
  /**
   * Enable geolocation control
   * @default true
   */
  showGeolocate?: boolean
  /**
   * Enable fullscreen control
   * @default true
   */
  showFullscreen?: boolean
  /**
   * Enable scale control
   * @default true
   */
  showScale?: boolean
  /**
   * Map style URL
   * @default 'mapbox://styles/mapbox/light-v11'
   */
  mapStyle?: string
  /**
   * Hecho seleccionado desde fuera (para sincronizar con la lista)
   */
  selectedHechoId?: string
  /**
   * Callback cuando se selecciona un hecho
   */
  onHechoSelect?: (hecho: HechoDTO | null) => void
  /**
   * Callback cuando se hace clic en "Ver detalles"
   */
  onHechoDetails?: (hecho: HechoDTO) => void
  /**
   * Hecho con hover desde la lista
   */
  hoveredHechoId?: string
}

// Mapa de categorías a colores
const getCategoriaColor = (categoria: string): string => {
  switch (categoria) {
    case "INCENDIO":
      return "#f31260" // danger
    case "CONTAMINACION":
      return "#f5a524" // warning
    case "MANIFESTACION":
      return "#006FEE" // primary
    case "INUNDACION":
      return "#3b82f6" // blue
    case "FAUNA":
      return "#17c964" // success
    default:
      return "#9353d3" // secondary
  }
}

export function MapComponent({
  hechos = [],
  initialLongitude = -58.3816,
  initialLatitude = -34.6037,
  initialZoom = 10,
  height = '100%',
  width = '100%',
  showControls = true,
  showGeolocate = true,
  showFullscreen = true,
  showScale = true,
  mapStyle = 'mapbox://styles/mapbox/light-v11',
  selectedHechoId,
  onHechoSelect,
  onHechoDetails,
  hoveredHechoId,
  ...mapProps
}: MapComponentProps) {
  const [selectedHecho, setSelectedHecho] = useState<HechoDTO | null>(null)
  const mapRef = useMapResize()

  // Sincronizar con selección externa
  const handleHechoClick = useCallback((hecho: HechoDTO) => {
    setSelectedHecho(hecho)
    onHechoSelect?.(hecho)
  }, [onHechoSelect])

  // Calcular el centro y zoom basado en los hechos si existen
  const { center, zoom } = useMemo(() => {
    if (hechos.length === 0) {
      return {
        center: { longitude: initialLongitude, latitude: initialLatitude },
        zoom: initialZoom
      }
    }

    // Calcular bounds de todos los hechos
    const lngs = hechos.map(h => h.longitud)
    const lats = hechos.map(h => h.latitud)

    const minLng = Math.min(...lngs)
    const maxLng = Math.max(...lngs)
    const minLat = Math.min(...lats)
    const maxLat = Math.max(...lats)

    // Calcular centro
    const centerLng = (minLng + maxLng) / 2
    const centerLat = (minLat + maxLat) / 2

    // Calcular zoom aproximado basado en la extensión
    const lngDiff = maxLng - minLng
    const latDiff = maxLat - minLat
    const maxDiff = Math.max(lngDiff, latDiff)

    let calculatedZoom = 10
    if (maxDiff > 0) {
      calculatedZoom = Math.floor(Math.log2(360 / maxDiff)) - 1
      calculatedZoom = Math.max(2, Math.min(15, calculatedZoom))
    }

    return {
      center: { longitude: centerLng, latitude: centerLat },
      zoom: hechos.length === 1 ? 14 : calculatedZoom
    }
  }, [hechos, initialLongitude, initialLatitude, initialZoom])

  const [viewState, setViewState] = useState({
    longitude: center.longitude,
    latitude: center.latitude,
    zoom: zoom
  })

  // Guardar el estado inicial del mapa para volver cuando se deseleccione
  const [initialViewState, setInitialViewState] = useState({
    longitude: center.longitude,
    latitude: center.latitude,
    zoom: zoom
  })

  // Usar ref para rastrear si tenemos una selección activa
  const previousSelectionRef = useRef<string | undefined>(undefined)

  // Actualizar el estado inicial cuando cambian los hechos (sin selección activa)
  useEffect(() => {
    if (!selectedHechoId) {
      setInitialViewState({
        longitude: center.longitude,
        latitude: center.latitude,
        zoom: zoom
      })
    }
  }, [center.longitude, center.latitude, zoom, selectedHechoId])

  // Efecto para hacer zoom cuando se selecciona/deselecciona un hecho desde la lista
  useEffect(() => {
    if (!mapRef.current) return

    const previousSelection = previousSelectionRef.current

    if (selectedHechoId) {
      const hecho = hechos.find(h => h.uuid === selectedHechoId)
      if (hecho) {
        // Usar flyTo para una animación suave hacia el hecho seleccionado
        mapRef.current.flyTo({
          center: [hecho.longitud, hecho.latitud],
          zoom: 12, // Zoom más moderado
          duration: 1200,
          essential: true
        })
      }
    } else if (previousSelection) {
      // Solo hacer zoom out si había una selección previa
      mapRef.current.flyTo({
        center: [initialViewState.longitude, initialViewState.latitude],
        zoom: initialViewState.zoom,
        duration: 1200,
        essential: true
      })
    }

    // Actualizar la referencia
    previousSelectionRef.current = selectedHechoId
  }, [selectedHechoId, hechos, initialViewState])

  const mapboxToken = process.env.NEXT_PUBLIC_MAPBOX_TOKEN

  if (!mapboxToken) {
    return (
      <div
        className="w-full h-full bg-muted flex items-center justify-center border border-destructive"
        style={{ height, width }}
      >
        <div className="text-center space-y-2 p-4">
          <div className="text-4xl">⚠️</div>
          <p className="text-destructive font-semibold">Mapbox Token Missing</p>
          <p className="text-sm text-muted-foreground">
            Please add NEXT_PUBLIC_MAPBOX_TOKEN to your .env.local file
          </p>
        </div>
      </div>
    )
  }

  return (
    <div style={{ height, width }} className="w-full h-full">
      <Map
        ref={mapRef}
        {...viewState}
        {...mapProps}
        onMove={(evt) => setViewState(evt.viewState)}
        mapboxAccessToken={mapboxToken}
        mapStyle={mapStyle}
        style={{ width: '100%', height: '100%' }}
      >
        {/* Marcadores de hechos */}
        {hechos.map((hecho) => {
          const isSelected = selectedHechoId === hecho.uuid || selectedHecho?.uuid === hecho.uuid
          const isHovered = hoveredHechoId === hecho.uuid
          const markerColor = getCategoriaColor(hecho.categoria)

          return (
            <Marker
              key={hecho.uuid}
              longitude={hecho.longitud}
              latitude={hecho.latitud}
              anchor="bottom"
              onClick={(e) => {
                e.originalEvent.stopPropagation()
                handleHechoClick(hecho)
              }}
            >
              <div
                className={`cursor-pointer transition-all duration-200 ${
                  isSelected ? 'scale-125 z-10' : isHovered ? 'scale-110' : 'scale-100'
                }`}
                style={{
                  width: '36px',
                  height: '36px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  position: 'relative'
                }}
              >
                {/* Pulse effect para seleccionados */}
                {isSelected && (
                  <div
                    className="absolute inset-0 rounded-full animate-ping"
                    style={{
                      backgroundColor: markerColor,
                      opacity: 0.4
                    }}
                  />
                )}

                {/* Marcador principal */}
                <svg
                  width="36"
                  height="36"
                  viewBox="0 0 24 24"
                  fill={markerColor}
                  stroke={markerColor}
                  strokeWidth="1"
                  style={{
                    filter: `drop-shadow(0 4px 8px rgba(0,0,0,0.4))`,
                    position: 'relative',
                    zIndex: isSelected ? 10 : 1
                  }}
                >
                  <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7zm0 9.5c-1.38 0-2.5-1.12-2.5-2.5s1.12-2.5 2.5-2.5 2.5 1.12 2.5 2.5-1.12 2.5-2.5 2.5z" />
                </svg>

                {/* Badge de contador si tiene imágenes */}
                {hecho.multimedia && hecho.multimedia.filter(url => /\.(jpg|jpeg|png|gif|webp)$/i.test(url)).length > 0 && (
                  <div
                    className="absolute -top-1 -right-1 w-4 h-4 rounded-full bg-white text-black text-xs flex items-center justify-center font-bold"
                    style={{ fontSize: '10px', zIndex: 11 }}
                  >
                    {hecho.multimedia.filter(url => /\.(jpg|jpeg|png|gif|webp)$/i.test(url)).length}
                  </div>
                )}
              </div>
            </Marker>
          )
        })}

        {/* Popup al seleccionar un hecho */}
        <AnimatePresence>
          {selectedHecho && (
            <Popup
              longitude={selectedHecho.longitud}
              latitude={selectedHecho.latitud}
              anchor="bottom"
              onClose={() => setSelectedHecho(null)}
              closeButton={true}
              closeOnClick={false}
              maxWidth="340px"
              className="mapbox-popup-custom"
            >
              <motion.div
                initial={{ opacity: 0, scale: 0.8, y: 10 }}
                animate={{ opacity: 1, scale: 1, y: 0 }}
                exit={{ opacity: 0, scale: 0.8, y: 10 }}
                transition={{
                  duration: 0.3,
                  scale: { type: "spring", visualDuration: 0.3, bounce: 0.3 }
                }}
                className="min-w-[300px] max-w-[340px] p-4"
              >
                <h3 className="font-bold text-lg mb-2 text-foreground line-clamp-2 pr-6">
                  {selectedHecho.titulo}
                </h3>
                <p className="text-sm text-default-600 mb-4 line-clamp-3">
                  {selectedHecho.descripcion}
                </p>
                <div className="flex flex-wrap gap-2 mb-4">
                  <span
                    className="inline-flex items-center rounded-full px-3 py-1.5 text-xs font-bold text-white shadow-sm"
                    style={{ backgroundColor: getCategoriaColor(selectedHecho.categoria) }}
                  >
                    {selectedHecho.categoria}
                  </span>
                </div>
                <div className="flex items-center justify-between gap-3">
                  <div className="flex items-center gap-2 text-xs text-default-500">
                    <span className="text-base">📅</span>
                    <span>
                      {new Date(selectedHecho.fechaAcontecimiento).toLocaleDateString('es-AR', {
                        day: '2-digit',
                        month: 'short',
                        year: 'numeric'
                      })}
                    </span>
                  </div>
                  <motion.button
                    onClick={() => {
                      onHechoDetails?.(selectedHecho)
                    }}
                    className="popup-details-button bg-primary/10 hover:bg-primary/20 text-primary"
                    whileHover={{ scale: 1.05 }}
                    whileTap={{ scale: 0.95 }}
                  >
                    <i className="ri-fullscreen-line w-3.5 h-3.5" />
                    <span>Ver detalles</span>
                  </motion.button>
                </div>
              </motion.div>
            </Popup>
          )}
        </AnimatePresence>

        {/* Controles */}
        {showControls && (
          <NavigationControl position="top-right" />
        )}
        {showGeolocate && (
          <GeolocateControl position="top-right" />
        )}
        {showFullscreen && (
          <FullscreenControl position="top-right" />
        )}
        {showScale && (
          <ScaleControl />
        )}
      </Map>
    </div>
  )
}

// Export types for consumers
export type { MapComponentProps }
export { Marker, Map as ReactMapGL }

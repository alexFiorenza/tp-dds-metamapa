import { useEffect, useRef } from 'react'

/**
 * Hook personalizado para manejar el redimensionamiento del mapa
 * cuando cambia el tamaño del contenedor o el estado del sidebar
 */
export function useMapResize() {
  const mapRef = useRef<any>(null)

  useEffect(() => {
    const handleResize = () => {
      if (mapRef.current) {
        // Usar requestAnimationFrame para asegurar que el DOM se haya actualizado
        requestAnimationFrame(() => {
          mapRef.current?.resize()
        })
      }
    }

    // Usar ResizeObserver para detectar cambios en el tamaño del contenedor
    const resizeObserver = new ResizeObserver(handleResize)
    const mapContainer = document.querySelector('.mapboxgl-map')
    
    if (mapContainer) {
      resizeObserver.observe(mapContainer)
    }

    // También escuchar eventos de resize de la ventana
    window.addEventListener('resize', handleResize)

    return () => {
      resizeObserver.disconnect()
      window.removeEventListener('resize', handleResize)
    }
  }, [])

  return mapRef
}

import { useEffect, useRef, useCallback } from 'react'

/**
 * Hook personalizado para manejar el redimensionamiento del mapa
 * cuando cambia el tamaño del contenedor o el estado del sidebar
 */
export function useMapResize() {
  const mapRef = useRef<any>(null)
  const resizeObserverRef = useRef<ResizeObserver | null>(null)

  const handleResize = useCallback(() => {
    if (mapRef.current) {
      // Usar requestAnimationFrame para asegurar que el DOM se haya actualizado
      requestAnimationFrame(() => {
        try {
          mapRef.current?.resize()
        } catch (error) {
          // Ignorar errores de resize (puede ocurrir si el mapa no está completamente cargado)
          console.debug('Map resize error:', error)
        }
      })
    }
  }, [])

  useEffect(() => {
    // Llamar a resize después de que el mapa esté montado
    const timeouts = [0, 100, 300, 500].map(delay =>
      setTimeout(handleResize, delay)
    )

    // Configurar ResizeObserver cuando el mapa esté listo
    const setupResizeObserver = () => {
      if (!resizeObserverRef.current) {
        resizeObserverRef.current = new ResizeObserver(handleResize)
      }

      // Intentar obtener el contenedor del mapa
      const mapContainer = mapRef.current?.getMap()?.getContainer()?.parentElement

      if (mapContainer) {
        resizeObserverRef.current.observe(mapContainer)
        return true
      }
      return false
    }

    // Intentar configurar ResizeObserver, reintentando si es necesario
    let retries = 0
    const maxRetries = 5
    const setupInterval = setInterval(() => {
      if (setupResizeObserver() || retries >= maxRetries) {
        clearInterval(setupInterval)
      }
      retries++
    }, 100)

    // Escuchar eventos de resize de la ventana
    window.addEventListener('resize', handleResize)

    return () => {
      timeouts.forEach(clearTimeout)
      clearInterval(setupInterval)
      resizeObserverRef.current?.disconnect()
      window.removeEventListener('resize', handleResize)
    }
  }, [handleResize])

  return mapRef
}

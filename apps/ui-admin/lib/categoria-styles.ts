/**
 * Helper para obtener estilos de categorías dinámicamente desde variables de entorno
 */

// Obtener categorías desde variable de entorno
const CATEGORIAS = (process.env.NEXT_PUBLIC_CATEGORIAS || "INCENDIO,CONTAMINACION,MANIFESTACION,INUNDACION,FAUNA,ALUD,OTRO").split(",");

// Mapeo de categorías a estilos (iconos y colores)
const CATEGORIA_STYLES: Record<string, { iconClass: string; color: "danger" | "warning" | "primary" | "secondary" | "success" | "default" }> = {
  INCENDIO: { iconClass: "ri-fire-line", color: "danger" },
  CONTAMINACION: { iconClass: "ri-drop-line", color: "warning" },
  MANIFESTACION: { iconClass: "ri-group-line", color: "primary" },
  INUNDACION: { iconClass: "ri-water-flash-line", color: "secondary" },
  FAUNA: { iconClass: "ri-plant-line", color: "success" },
  ALUD: { iconClass: "ri-snowy-line", color: "danger" },
  OTRO: { iconClass: "ri-map-pin-line", color: "default" },
};

/**
 * Obtiene los estilos (icono y color) para una categoría dada
 * @param categoria - Nombre de la categoría
 * @returns Objeto con iconClass y color, o valores por defecto si la categoría no está mapeada
 */
export function getCategoriaStyle(categoria: string) {
  // Buscar en el mapeo
  const style = CATEGORIA_STYLES[categoria.toUpperCase()];
  
  if (style) {
    return style;
  }
  
  return { iconClass: "ri-map-pin-line", color: "default" as const };
}

/**
 * Obtiene todas las categorías disponibles desde variables de entorno
 * @returns Array de strings con los nombres de las categorías
 */
export function getCategorias(): string[] {
  return CATEGORIAS;
}

/**
 * Verifica si una categoría es válida (existe en la lista de categorías)
 * @param categoria - Nombre de la categoría a verificar
 * @returns true si la categoría es válida, false en caso contrario
 */
export function isCategoriaValida(categoria: string): boolean {
  return CATEGORIAS.includes(categoria.toUpperCase());
}


'use client';

import * as React from "react"
import { Chip, ChipProps } from "@heroui/react"
import { cn } from "@/lib/utils"

// Wrapper para mantener compatibilidad con la API anterior usando Chip de Hero UI
function Badge({
  className,
  variant = "solid",
  children,
  ...props
}: Omit<ChipProps, 'variant'> & {
  variant?: 'default' | 'secondary' | 'destructive' | 'outline'
}) {
  // Mapeo de variantes custom a Hero UI Chip
  const chipVariant: ChipProps['variant'] =
    variant === 'outline' ? 'bordered' :
    variant === 'default' ? 'solid' :
    variant === 'secondary' ? 'flat' :
    'solid';

  const chipColor: ChipProps['color'] =
    variant === 'destructive' ? 'danger' :
    variant === 'secondary' ? 'secondary' :
    'primary';

  return (
    <Chip
      variant={chipVariant}
      color={chipColor}
      size="sm"
      className={cn(className)}
      {...props}
    >
      {children}
    </Chip>
  )
}

export { Badge }

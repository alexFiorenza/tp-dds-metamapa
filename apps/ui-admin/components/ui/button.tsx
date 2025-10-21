'use client';

import * as React from "react"
import { Button as HeroButton, ButtonProps as HeroButtonProps } from "@heroui/react"
import { cn } from "@/lib/utils"

// Wrapper para mantener compatibilidad con la API anterior
function Button({
  className,
  variant = "solid",
  size = "md",
  children,
  ...props
}: Omit<HeroButtonProps, 'variant' | 'size'> & {
  variant?: 'default' | 'destructive' | 'outline' | 'secondary' | 'ghost' | 'link' | HeroButtonProps['variant']
  size?: 'default' | 'sm' | 'lg' | 'icon' | 'icon-sm' | 'icon-lg' | HeroButtonProps['size']
}) {
  // Mapeo de variantes custom a Hero UI
  const heroVariant: HeroButtonProps['variant'] =
    variant === 'default' ? 'solid' :
    variant === 'destructive' ? 'solid' :
    variant === 'outline' ? 'bordered' :
    variant === 'secondary' ? 'flat' :
    variant === 'ghost' ? 'light' :
    variant === 'link' ? 'light' :
    variant as HeroButtonProps['variant'];

  // Mapeo de tamaños
  const heroSize: HeroButtonProps['size'] =
    size === 'default' ? 'md' :
    size === 'icon' || size === 'icon-sm' || size === 'icon-lg' ? 'md' :
    size as HeroButtonProps['size'];

  const isIconButton = size === 'icon' || size === 'icon-sm' || size === 'icon-lg';

  return (
    <HeroButton
      variant={heroVariant}
      size={heroSize}
      color={variant === 'destructive' ? 'danger' : 'primary'}
      isIconOnly={isIconButton}
      className={cn(className)}
      {...props}
    >
      {children}
    </HeroButton>
  )
}

export { Button }

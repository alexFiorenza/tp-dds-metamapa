'use client';

import * as React from "react"
import { Input as HeroInput, InputProps } from "@heroui/react"
import { cn } from "@/lib/utils"

// Wrapper para mantener compatibilidad con la API anterior
function Input({ className, ...props }: InputProps) {
  return (
    <HeroInput
      classNames={{
        input: cn(className),
      }}
      {...props}
    />
  )
}

export { Input }

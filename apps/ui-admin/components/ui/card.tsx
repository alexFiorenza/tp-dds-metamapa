'use client';

import * as React from "react"
import { Card as HeroCard, CardBody, CardFooter as HeroCardFooter, CardHeader as HeroCardHeader } from "@heroui/react"
import { cn } from "@/lib/utils"

// Wrapper para mantener compatibilidad con la API anterior
function Card({ className, children, onClick, ...props }: React.ComponentProps<"div">) {
  return (
    <HeroCard
      className={cn(className)}
      isPressable={!!onClick}
      onPress={onClick as any}
      {...props}
    >
      {children}
    </HeroCard>
  )
}

function CardHeader({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <HeroCardHeader
      className={cn(className)}
      {...props}
    />
  )
}

function CardTitle({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("leading-none font-semibold", className)}
      {...props}
    />
  )
}

function CardDescription({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn("text-default-500 text-sm", className)}
      {...props}
    />
  )
}

function CardAction({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <div
      className={cn(
        "col-start-2 row-span-2 row-start-1 self-start justify-self-end",
        className
      )}
      {...props}
    />
  )
}

function CardContent({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <CardBody
      className={cn(className)}
      {...props}
    />
  )
}

function CardFooter({ className, ...props }: React.ComponentProps<"div">) {
  return (
    <HeroCardFooter
      className={cn("flex items-center", className)}
      {...props}
    />
  )
}

export {
  Card,
  CardHeader,
  CardFooter,
  CardTitle,
  CardAction,
  CardDescription,
  CardContent,
}

import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"

export function HechoCardSkeleton() {
  return (
    <Card className="overflow-hidden">
      <Skeleton className="w-full h-40" />
      <div className="p-4 space-y-2">
        <div className="flex items-start justify-between gap-2">
          <Skeleton className="h-5 w-3/4" />
          <Skeleton className="h-5 w-16" />
        </div>
        <Skeleton className="h-4 w-full" />
        <Skeleton className="h-4 w-2/3" />
        <div className="space-y-1">
          <Skeleton className="h-3 w-32" />
          <Skeleton className="h-3 w-40" />
        </div>
        <div className="flex gap-1">
          <Skeleton className="h-5 w-16" />
          <Skeleton className="h-5 w-20" />
        </div>
      </div>
    </Card>
  )
}

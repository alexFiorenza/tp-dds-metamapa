import { Card } from "@/components/ui/card"
import { Skeleton } from "@/components/ui/skeleton"

export function ColeccionCardSkeleton() {
  return (
    <Card className="p-4">
      <div className="flex items-start gap-3">
        <Skeleton className="h-9 w-9 rounded-lg" />
        <div className="flex-1 space-y-2">
          <Skeleton className="h-4 w-3/4" />
          <Skeleton className="h-3 w-full" />
          <Skeleton className="h-3 w-1/2" />
          <Skeleton className="h-3 w-24 mt-2" />
        </div>
      </div>
    </Card>
  )
}

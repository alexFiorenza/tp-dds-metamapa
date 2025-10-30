import type { ReactNode } from "react"

interface SidebarLayoutProps {
  children: ReactNode
  sidebar: ReactNode
}

export function SidebarLayout({ children, sidebar }: SidebarLayoutProps) {
  return (
    <div className="flex h-screen overflow-hidden bg-background">
      {/* Sidebar */}
      <aside className="w-[400px] border-r border-border bg-card flex flex-col overflow-hidden">{sidebar}</aside>

      {/* Main content (mapa) */}
      <main className="flex-1 relative overflow-hidden">{children}</main>
    </div>
  )
}

import type { Metadata } from 'next'
import { Analytics } from '@vercel/analytics/next'
import { ClerkProvider } from '@clerk/nextjs'
import './globals.css'
import 'remixicon/fonts/remixicon.css'
import { Providers } from '@/components/providers'
import { LayoutWrapper } from '@/components/layout-wrapper'
import { Lato } from 'next/font/google'

// Initialize Lato font
const lato = Lato({
  subsets: ['latin'],
  weight: ['100', '300', '400', '700', '900'],
  variable: '--font-lato'
})

export const metadata: Metadata = {
  title: 'MetaMapa Admin',
  description: 'Sistema de administración MetaMapa',
}

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode
}>) {
  return (
    <ClerkProvider>
      <html lang="es" suppressHydrationWarning>
        <body className={`${lato.variable} font-sans antialiased h-screen overflow-hidden`}>
          <Providers>
            <LayoutWrapper>
              {children}
            </LayoutWrapper>
            <Analytics />
          </Providers>
        </body>
      </html>
    </ClerkProvider>
  )
}

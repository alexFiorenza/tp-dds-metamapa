import { auth } from '@clerk/nextjs/server';
import { redirect } from 'next/navigation';

export default async function AdminLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  const { userId, sessionClaims } = await auth();

  // If no user, middleware should have redirected, but double check
  if (!userId) {
    redirect('/');
  }

  // Check if user has admin role
  // You can set roles in Clerk Dashboard under "Sessions" -> "Customize session token"
  // Add a custom claim like: { "metadata": { "role": "admin" } }
  // Or use Clerk's organizations/roles feature
  const userRole = sessionClaims?.metadata?.role as string | undefined;

  if (userRole !== 'admin') {
    return (
      <div className="flex items-center justify-center min-h-screen p-4">
        <div className="max-w-md w-full bg-red-50 border border-red-200 rounded-lg p-6">
          <h1 className="text-2xl font-bold text-red-800 mb-2">
            Acceso Denegado
          </h1>
          <p className="text-red-700 mb-4">
            No tienes permisos de administrador para acceder a esta sección.
          </p>
          <a
            href="/"
            className="inline-block px-4 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition-colors"
          >
            Volver al inicio
          </a>
        </div>
      </div>
    );
  }

  return <>{children}</>;
}

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MetaMapa is a full-stack system for managing and aggregating data from various sources with monitoring capabilities.

### Architecture

- **Backend**: Multi-module Maven project with shared domain
- **Microservices architecture** with 6 independent Java services
- **Javalin-based REST APIs** for each microservice
- **Frontend**: Next.js 15 admin application with TypeScript
- **Docker containerization** with compose orchestration
- **Monitoring stack** (Prometheus, Grafana, Alertmanager)

### Key Technologies

**Backend:**
- Java 17
- Maven (multi-module project)
- Javalin web framework
- Hibernate ORM with PostgreSQL
- Docker & Docker Compose
- Prometheus, Grafana, Alertmanager for monitoring

**Frontend:**
- Next.js 15 (React 19) with TypeScript
- HeroUI component library
- Tailwind CSS v4
- Mapbox GL for map visualization
- Clerk.js for authentication

## Development Commands

### Build and Compilation
```bash
# Compile entire project
mvn clean compile

# Build JARs for all modules
mvn clean package

# Skip tests during build
mvn clean package -DskipTests
```

### Running Services
```bash
# Run specific microservice (example: agregador)
cd apps/agregador
mvn exec:java -Dexec.mainClass="utn.dds.agregador.Main"

# Run MetaMapa service
cd apps/metamapa  
mvn exec:java -Dexec.mainClass="utn.dds.metamapa.Main"
```

### Docker Operations
```bash
# Build all Docker images and JARs
./build-and-dockerize.sh

# Start all services with monitoring stack
docker-compose up -d

# Stop all services
docker-compose down
```

### Frontend Development (ui-admin)
```bash
# Navigate to ui-admin
cd apps/ui-admin

# Install dependencies
yarn install

# Run development server (port 3000)
yarn dev

# Build production
yarn build

# Run production server
yarn start

# Type checking
yarn type-check

# Linting
yarn lint
```

### Testing
```bash
# Backend tests
mvn test

# Run tests for specific module
cd apps/{module-name}
mvn test
```

## Project Structure

### Modules Layout
```
apps/
├── shared/           # Domain models, repositories, strategies
├── fuentes/         # Data source microservices
│   ├── estatica/    # Static data sources (port 7001)
│   ├── dinamica/    # Dynamic data sources (port 7002)
│   └── proxy/       # Proxy services
│       ├── metamapa/ # MetaMapa proxy (port 7003)
│       └── demo/     # Demo proxy (port 7004)
├── agregador/       # Data aggregation service (port 7005)
├── metamapa/        # Main MetaMapa service (port 7006)
├── ui-admin/        # Next.js admin frontend (port 3000)
│   ├── app/         # Next.js 15 App Router pages
│   ├── components/  # React components (UI, filters, maps)
│   ├── lib/         # API client and utilities
│   ├── types/       # TypeScript type definitions
│   └── hooks/       # Custom React hooks
└── estadisticas/    # Monitoring configuration
```

### Domain Architecture
The `apps/shared` module contains:
- **Domain entities**: `Hecho`, `Contribuyente`, `Coleccion`
- **Enums**: `EstadoHecho`, `TipoHecho`, `Origen`
- **Strategy patterns**: For categorization and data processing
- **Repository abstractions**: `IDAO`, `FuenteRepository`
- **Data source interfaces**: `FuenteDeDatos`, `FuenteEstatica`

### Service Ports
- Fuentes Estáticas: 7001
- Fuentes Dinámicas: 7002
- Proxy MetaMapa: 7003
- Proxy Demo: 7004
- Agregador: 7005
- MetaMapa: 7006
- UI Admin: 3000
- Prometheus: 9090
- Alertmanager: 9093
- Grafana: 3000

## UI Admin Application

### Overview
The `apps/ui-admin` is a Next.js 15 admin interface built with React 19 and TypeScript. It provides a modern web interface for managing and visualizing hechos (facts/events) with advanced filtering, map visualization, and collection management.

### Key Features
- **Interactive map visualization** using Mapbox GL with synchronized list view
- **Advanced filtering system** with URL synchronization for shareable filtered views
- **Role-based access control** using Clerk authentication
- **Collection management** for organizing hechos
- **Deletion request workflow** for content moderation
- **Responsive design** with dark/light theme support

### Application Structure

#### Pages and Routes
- `/hechos` - Main hechos view with map and filters (public)
- `/colecciones` - Public collections listing
- `/colecciones/[handle]` - Individual collection detail with hechos
- `/solicitudes` - Deletion requests page (public)
- `/admin` - Admin dashboard (requires authentication)
- `/administrador/*` - Protected admin-only routes

#### Core Components
**Filtering System:**
- `components/filtros-hechos.tsx` - Main filtering UI component with basic and advanced filters
- Supports filtering by: categoria, titulo, descripcion, origen, estado, fechaAcontecimiento, etiquetas, coordenadas (lat/lng)
- **URL synchronization**: All filters are reflected in browser URL for shareable links

**Data Visualization:**
- `components/hechos-map-view.tsx` - Combined map and list view
- `components/map.tsx` - Mapbox GL integration with marker clustering
- `components/hecho-card.tsx` / `hecho-list-item.tsx` - Display components

**API Integration:**
- `lib/api-client.ts` - Centralized API client for backend communication
- `app/api/hechos/route.ts` - Next.js API route that proxies to Java backend (port 7006)
- Mock data support via `NEXT_PUBLIC_USE_MOCK` environment variable

### Filtering Implementation

The filtering system has three layers:

1. **UI Layer** (`components/filtros-hechos.tsx`):
   - Provides form inputs for all filter parameters
   - Auto-expands advanced filters if URL contains advanced filter params
   - Synchronizes local state with `filtrosIniciales` prop

2. **Page Layer** (`app/hechos/page.tsx`):
   - Reads filters from URL using `useSearchParams()`
   - Updates URL when filters change using `useRouter()`
   - Maintains filter state and passes to API client
   - Enables shareable filtered views via URL

3. **API Layer** (`lib/api-client.ts`):
   - Converts filter object to URLSearchParams
   - Forwards to `/api/hechos` Next.js route
   - Next.js route proxies to Java backend at `http://localhost:7006/api/hechos`

**Supported Filter Parameters:**
```typescript
interface FiltrosHechos {
  categoria?: string           // INCENDIO, CONTAMINACION, MANIFESTACION, INUNDACION, FAUNA
  titulo?: string             // Text search in title
  descripcion?: string        // Text search in description
  origen?: string             // Source/origin filter
  fechaAcontecimiento?: string // Event date (YYYY-MM-DD)
  longitud?: number           // Longitude (exact match)
  latitud?: number            // Latitude (exact match)
  estado?: EstadoHecho        // ACTIVO | OCULTO
  fechaCarga?: string         // Load date (YYYY-MM-DDTHH:mm:ss)
  etiquetas?: string          // Comma-separated tags
  pagina?: number             // Page number (0-indexed)
  tamanioPagina?: number      // Page size (default: 10, max: 100)
}
```

**Example Filtered URL:**
```
/hechos?categoria=INCENDIO&estado=ACTIVO&titulo=fuego
```

### Backend API Integration

The Java backend (`apps/metamapa`) accepts all filter parameters as query params:
- Endpoint: `GET /api/hechos`
- Controller: `ControllerHechoPublico.java`
- Filters created via `FiltroFactory.crearFiltros(ctx)`
- Returns: `RespuestaPaginadaDTO<HechoDTO>`

### Environment Configuration

Required environment variables (`.env.local`):
```bash
# Backend API
NEXT_PUBLIC_API_URL=http://localhost:7006

# Mock mode (optional, for development)
NEXT_PUBLIC_USE_MOCK=false

# Clerk authentication
NEXT_PUBLIC_CLERK_PUBLISHABLE_KEY=pk_test_...
CLERK_SECRET_KEY=sk_test_...

# Mapbox (for map visualization)
NEXT_PUBLIC_MAPBOX_TOKEN=pk.ey...
```

### Development Workflow

1. **Start the Java backend** (port 7006):
   ```bash
   cd apps/metamapa
   mvn exec:java -Dexec.mainClass="utn.dds.metamapa.Main"
   ```

2. **Start the Next.js dev server**:
   ```bash
   cd apps/ui-admin
   yarn dev  # Runs on port 3000
   ```

3. **Access the application**:
   - Navigate to `http://localhost:3000/hechos`
   - Apply filters and verify URL updates
   - Share filtered URLs with others

### Type Safety

All DTOs are typed in `types/api.ts`:
- `HechoDTO` - Individual hecho entity
- `ColeccionDTO` - Collection with membership criteria
- `SolicitudEliminacionDTO` - Deletion request
- `RespuestaPaginadaDTO<T>` - Paginated response wrapper
- `FiltrosHechos` - Filter parameters interface

### Authentication & Authorization

- **Provider**: Clerk.js
- **Public routes**: `/hechos`, `/colecciones`, `/solicitudes`
- **Protected routes**: `/administrador/*`
- **Role check**: `useUserRole()` hook checks `publicMetadata.role === "admin"`
- **Admin sidebar**: Only visible to authenticated admins

## Monitoring and Metrics

### Stack Components
- **Prometheus**: Scrapes `/metrics` endpoints from all microservices
- **Alertmanager**: Sends alerts to `proxy-metamapa:7003/consenso` endpoint
- **Grafana**: Dashboards at http://localhost:3000 (admin/admin)

### Integration Pattern
Each microservice should expose a `/health` endpoint and `/metrics` endpoint for monitoring.

## Development Notes

- All microservices use **Javalin** framework
- Maven Shade plugin creates **fat JARs** for deployment
- Each service has its own **Dockerfile** for containerization
- **Shared domain logic** prevents code duplication across services
- Alert integration sends HTTP POST to proxy services for consensus handling
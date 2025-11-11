# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MetaMapa is a full-stack system for managing and aggregating data from various sources with monitoring capabilities.

### Architecture

- **Backend**: Multi-module Maven project with shared domain
- **Microservices architecture** with 6 independent Java services + 1 TypeScript service
- **Javalin-based REST APIs** for each Java microservice
- **Normalization service**: TypeScript/Node.js serverless function (Railway-ready)
- **Frontend**: Next.js 15 admin application with TypeScript
- **Docker containerization** with compose orchestration
- **Monitoring stack** (Prometheus, Grafana, Alertmanager)
- **Documentation**: Mermaid.js diagram viewer at `apps/docs`

### IMPORTANT: Architecture Documentation and Compliance

**All architectural decisions and code changes MUST strictly follow the diagrams defined in `apps/docs/diagramas/`.**

The `apps/docs/diagramas/` directory contains all project architecture diagrams in Mermaid format (.mmd files).

**Compliance Rules:**
1. **ALWAYS consult relevant diagrams in `apps/docs/diagramas/`** before making architectural changes
2. **ANY deviation from documented architecture MUST be flagged to the user** before implementation
3. If a change requires updating the architecture, **explicitly notify the user** and suggest updating the corresponding `.mmd` file
4. When adding new components, endpoints, or services, **verify they align** with existing architecture patterns shown in the diagrams
5. If diagrams are outdated or conflicting with code, **alert the user immediately**

To view and edit diagrams:
```bash
cd apps/docs
npm install
npm start
# Open http://localhost:8080
```

### Key Technologies

**Backend (Java):**
- Java 17
- Maven (multi-module project)
- Javalin web framework
- Hibernate ORM with PostgreSQL
- Docker & Docker Compose
- Prometheus, Grafana, Alertmanager for monitoring

**Normalizador (TypeScript):**
- Node.js 20+
- TypeScript with ES Modules
- Native HTTP (no frameworks - ultra-lightweight)
- Levenshtein distance for fuzzy matching
- Pipeline architecture with 5 sequential normalizers
- Configurable category mappings via JSON

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

**Java Services:**
```bash
# Run specific microservice (example: agregador)
cd apps/agregador
mvn exec:java -Dexec.mainClass="utn.dds.agregador.Main"

# Run MetaMapa service
cd apps/metamapa
mvn exec:java -Dexec.mainClass="utn.dds.metamapa.Main"
```

**Normalizador (TypeScript):**
```bash
# Navigate to normalizador
cd apps/normalizador

# Install dependencies (first time)
npm install

# Run development server with hot reload
npm run dev

# Build for production
npm run build

# Run production server
PORT=3005 npm start
```

### Docker Operations

**IMPORTANT: All Docker images are built for AMD64 (linux/amd64) architecture to ensure compatibility across different platforms.**

```bash
# Build all Docker images and JARs locally
./build-and-dockerize.sh

# Start all services with monitoring stack
docker-compose up -d

# Stop all services
docker-compose down

# Build for specific platform (if needed)
docker buildx build --platform linux/amd64 -t image-name .
```

**Platform Configuration:**
- All services in `docker-compose.yml` specify `platform: linux/amd64`
- GitHub Actions workflows build images with `platforms: linux/amd64`
- This ensures consistency across ARM (Apple Silicon) and AMD64 systems

### CI/CD Pipeline

The project uses GitHub Actions for automated build and deployment:

**Workflow Files:**
- `.github/workflows/develop.yml` - Develop branch CI/CD
- `.github/workflows/main.yml` - Main branch CI/CD

**Automated Process:**
1. Trigger on push to `develop` or `main` branches
2. Build project with Maven (`mvn clean package -DskipTests`)
3. Build Docker images for all microservices
4. Push images to Docker Hub with appropriate tags

**Docker Image Tags:**
- `develop` branch → `:experimental` tag
- `main` branch → `:latest` tag

**Required GitHub Secrets:**
- `DOCKERHUB_USERNAME` - Your Docker Hub username
- `DOCKERHUB_TOKEN` - Docker Hub access token

**Docker Hub Images:**
- `{username}/metamapa-fuentes-estatica`
- `{username}/metamapa-fuentes-dinamica`
- `{username}/metamapa-proxy-metamapa`
- `{username}/metamapa-proxy-demo`
- `{username}/metamapa-agregador`
- `{username}/metamapa-gestor`

**Notes:**
- Pull requests only run build and test, no push to Docker Hub
- Pushes to branches trigger full CI/CD pipeline with Docker push
- Build cache is used to speed up subsequent builds

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
├── normalizador/    # Data normalization service (port 3005, TypeScript)
│   ├── src/
│   │   ├── index.ts             # HTTP server entry point
│   │   ├── pipeline.ts          # Normalization pipeline orchestrator
│   │   ├── types.ts             # TypeScript type definitions
│   │   ├── normalizadores/      # 5 sequential normalizers
│   │   │   ├── texto.ts         # Text cleanup (trim, spaces)
│   │   │   ├── categoria.ts     # Category mapping (exact, regex, fuzzy)
│   │   │   ├── fecha.ts         # Date normalization (ISO-8601)
│   │   │   ├── coordenadas.ts   # Coordinate validation
│   │   │   └── etiquetas.ts     # Tag normalization
│   │   ├── mapeos/
│   │   │   └── categorias.json  # Configurable category mappings
│   │   └── utils/
│   │       └── similitud.ts     # Levenshtein distance algorithm
│   ├── Dockerfile               # Multi-stage Node.js build
│   ├── package.json
│   └── tsconfig.json
├── metamapa/        # Main MetaMapa service (port 7006)
├── ui-admin/        # Next.js admin frontend (port 3000)
│   ├── app/         # Next.js 15 App Router pages
│   ├── components/  # React components (UI, filters, maps)
│   ├── lib/         # API client and utilities
│   ├── types/       # TypeScript type definitions
│   └── hooks/       # Custom React hooks
├── docs/            # Mermaid.js diagram viewer (port 8080)
│   ├── diagramas/   # Architecture diagrams (.mmd files)
│   ├── public/      # Web interface
│   └── server.js    # Express server
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
- **Normalizador: 3005** (TypeScript/Node.js)
  - Health: `GET /health`
  - Normalize: `POST /normalizar`
- MetaMapa: 7006
- UI Admin: 3000
- Docs (Mermaid viewer): 8080
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

## Normalizador Service (TypeScript)

### Overview
The normalizador is a **stateless serverless function** built with TypeScript that normalizes hechos (facts) before they are persisted by the Agregador. It runs independently as a microservice and communicates via HTTP.

### Architecture
**Pipeline Pattern:** 5 sequential normalizers process each hecho:
1. **TextoNormalizador** (OPCIONAL) - Trims whitespace, normalizes line breaks
2. **CategoriaNormalizador** (ADVERTENCIA) - Maps categories using:
   - Exact matching (case-insensitive)
   - Regex patterns
   - Fuzzy matching (Levenshtein distance)
   - Fallback to "OTRO" if no match
3. **FechaNormalizador** (ADVERTENCIA) - Converts to ISO-8601, removes timezone
4. **CoordenadasNormalizador** (CRÍTICO) - Validates ranges, rounds to 6 decimals
5. **EtiquetasNormalizador** (OPCIONAL) - Lowercase, deduplicate, sort

**Severity Levels:**
- **CRÍTICO**: Error rejects the hecho completely
- **ADVERTENCIA**: Error generates warning, uses default value
- **OPCIONAL**: Error is silently ignored

### Key Features
- **Zero runtime dependencies** (only dev dependencies for TypeScript)
- **Native Node.js HTTP** (no Express/Fastify)
- **Configurable categories** via `src/mapeos/categorias.json`
- **Fuzzy matching** with Levenshtein distance algorithm
- **Railway Function ready** (serverless deployment)
- **Health check** endpoint for monitoring

### Integration with Agregador
The Agregador calls the normalizador via HTTP:

**Flow:**
1. Agregador obtains hechos from all sources
2. **→ Sends hechos to Normalizador** (`POST /normalizar`)
3. **← Receives normalized hechos + rejected hechos**
4. Agregador filters duplicates
5. Saves to PostgreSQL

**Environment Variables:**
- `NORMALIZADOR_URL` - URL of normalizador service (default: `http://localhost:3005`)
- `NORMALIZADOR_ENABLED` - Enable/disable normalization (default: `true`)

**Java Integration:**
- `NormalizadorClient.java` - HTTP client for communication
- `ConfiguracionNormalizacionDTO.java` - Configuration DTO
- `NormalizarRequestDTO.java` - Request DTO
- `NormalizarResponseDTO.java` - Response DTO with statistics

### Configuration
Categories are configured in `apps/normalizador/src/mapeos/categorias.json`:

```json
{
  "categoriasValidas": ["INCENDIO", "CONTAMINACION", ...],
  "exactos": {
    "fuego": "INCENDIO",
    "sismo": "TERREMOTO"
  },
  "regex": {
    ".*(incendio|fuego).*": "INCENDIO"
  }
}
```

To add new categories:
1. Add to `categoriasValidas` array
2. Add exact mappings to `exactos` object (optional)
3. Add regex patterns to `regex` object (optional)
4. Rebuild: `npm run build`
5. Restart service

### API Endpoints

**Health Check:**
```bash
GET /health
Response: { status: "ok", service: "metamapa-normalizador", version: "1.0.0", ... }
```

**Normalize:**
```bash
POST /normalizar
Content-Type: application/json

Request:
{
  "hechos": [{ uuid, titulo, categoria, ... }],
  "config": {
    "normalizarCategorias": true,
    "usarFuzzyMatching": true,
    "umbralSimilitud": 0.85,
    "rechazarCoordenadasInvalidas": true
  }
}

Response:
{
  "hechosNormalizados": [...],
  "hechosRechazados": [...],
  "estadisticas": {
    "totalProcesados": 10,
    "totalNormalizados": 9,
    "totalRechazados": 1,
    "cambiosPorTipo": { "categoria": 5, "fecha": 3 },
    "tiempoEjecucion": 15
  }
}
```

### Deployment

**Docker:**
- Multi-stage build (builder + production)
- Alpine Linux base image
- Non-root user for security
- Port 3000 (mapped to 3005 in docker-compose)

**Railway:**
- Deploy directly from Dockerfile
- Set `NODE_ENV=production` and `PORT=3000`
- Configure `NORMALIZADOR_URL` in Agregador

### Development Workflow

1. **Make changes** to normalizers or configuration
2. **Test locally:**
   ```bash
   cd apps/normalizador
   npm run dev
   curl http://localhost:3005/health
   ```
3. **Test integration:**
   - Start normalizador on port 3005
   - Start agregador (will connect automatically)
   - Trigger aggregation and check logs
4. **Deploy:**
   - Docker: `./build-and-dockerize.sh`
   - Railway: `railway up`

### Monitoring
The normalizador logs:
- Number of hechos processed
- Normalization statistics (processed, normalized, rejected)
- Execution time
- Details of rejected hechos with reasons

Check Agregador logs for normalization activity:
```bash
docker-compose logs agregador | grep -i normalizador
```

## Development Notes

- All Java microservices use **Javalin** framework
- Maven Shade plugin creates **fat JARs** for deployment
- Each service has its own **Dockerfile** for containerization
- **Shared domain logic** prevents code duplication across services
- Alert integration sends HTTP POST to proxy services for consensus handling
- **Normalizador** is the only TypeScript service (serverless architecture)
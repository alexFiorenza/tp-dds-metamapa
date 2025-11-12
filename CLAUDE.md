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

## Fuentes Services (AWS Lambda)

### Overview
The fuentes (data sources) services are **dual-mode Javalin applications** that can run both as traditional Docker containers and as AWS Lambda functions. They provide data to the Agregador from various sources (static CSV, dynamic APIs, proxies).

### Architecture: Dual-Mode Deployment

Each fuente supports two execution modes:

**1. Traditional Mode (Docker/Local):**
- Runs Javalin on dedicated port (7001-7004)
- Full Swagger/OpenAPI documentation available
- Health check endpoints exposed
- Direct HTTP server

**2. Serverless Mode (AWS Lambda):**
- Handler forwards requests to internal Javalin instance
- No Swagger/OpenAPI (reduces cold start time)
- No health check endpoints
- Triggered by API Gateway HTTP API

**Mode Detection:**
- Presence of `AWS_LAMBDA_FUNCTION_NAME` environment variable determines mode
- Docker: variable absent → starts HTTP server
- Lambda: variable present → silent initialization

### Services and Endpoints

All services share a single API Gateway with path-based routing:

**1. Fuente Estática** (`/estatica/*`)
- **Endpoints:** `GET /estatica/hechos`
- **Purpose:** Serves hechos from static CSV files stored in S3
- **Storage:** MinIO (local) or AWS S3 (Lambda)
- **Handler:** `utn.dds.fuentes.estatica.LambdaHandler`
- **Environment Variables:**
  - `DAO_TYPE`: Storage type (s3)
  - `S3_ENDPOINT`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`, `S3_BUCKET`

**2. Fuente Dinámica** (`/dinamica/*`)
- **Endpoints:** `GET /dinamica/hechos`, `POST /dinamica/hechos`
- **Purpose:** Serves and accepts hechos from CouchDB
- **Storage:** CouchDB (Railway for Lambda)
- **Handler:** `utn.dds.fuentes.dinamica.LambdaHandler`
- **Environment Variables:**
  - `DAO_TYPE`: Storage type (couchdb)
  - `COUCHDB_URL`, `COUCHDB_USER`, `COUCHDB_PASSWORD`, `COUCHDB_DB`

**3. Proxy MetaMapa** (`/metamapa/*`)
- **Endpoints:** `GET /metamapa/hechos`
- **Purpose:** Proxies requests to external MetaMapa API
- **Handler:** `utn.dds.fuentes.proxy.metamapa.LambdaHandler`
- **Environment Variables:**
  - `URL`: External MetaMapa API URL

**4. Proxy Demo** (`/demo/*`)
- **Endpoints:** `GET /demo/hechos`, `PUT /demo/hechos`
- **Purpose:** Demo proxy for testing with CouchDB storage
- **Storage:** CouchDB (Railway for Lambda)
- **Handler:** `utn.dds.fuentes.proxy.demo.LambdaHandler`
- **Environment Variables:**
  - `DAO_TYPE`: Storage type (couchdb)
  - `COUCHDB_URL`, `COUCHDB_USER`, `COUCHDB_PASSWORD`, `COUCHDB_DB`

### Deployment Workflow

**Local/Docker:**
```bash
# Build JARs
mvn clean package -DskipTests

# Start with docker-compose
docker-compose up -d

# Access endpoints
curl http://localhost:7001/hechos  # Fuente Estática
curl http://localhost:7002/hechos  # Fuente Dinámica
curl http://localhost:7003/hechos  # Proxy MetaMapa
curl http://localhost:7004/hechos  # Proxy Demo
```

**AWS Lambda (via GitHub Actions):**
```bash
# Automatic deployment on push to develop/main
git push origin develop  # Deploys to 'dev' stage
git push origin main     # Deploys to 'prod' stage

# Manual deployment with Serverless Framework
cd apps/fuentes
serverless deploy --stage dev

# View deployed endpoints
serverless info --stage dev
```

**GitHub Actions Workflow:**
- File: `.github/workflows/deploy-fuentes-lambda.yml`
- Triggers: Push to `develop`/`main` or changes in `apps/fuentes/**`
- Steps:
  1. Build all JARs with Maven
  2. Install Serverless Framework
  3. Deploy all 4 Lambda functions to AWS
  4. Configure environment variables from GitHub Secrets

### Required GitHub Secrets

**AWS Credentials:**
- `AWS_ACCESS_KEY_ID` - AWS access key
- `AWS_SECRET_ACCESS_KEY` - AWS secret key

**Fuente Estática (Dev/Prod):**
- `FUENTE_ESTATICA_DAO_TYPE_DEV` / `FUENTE_ESTATICA_DAO_TYPE_PROD`
- `S3_ENDPOINT_DEV` / `S3_ENDPOINT_PROD`
- `S3_PUBLIC_ENDPOINT_DEV` / `S3_PUBLIC_ENDPOINT_PROD`
- `S3_ACCESS_KEY_DEV` / `S3_ACCESS_KEY_PROD`
- `S3_SECRET_KEY_DEV` / `S3_SECRET_KEY_PROD`
- `S3_BUCKET_DEV` / `S3_BUCKET_PROD`

**Fuente Dinámica (Dev/Prod):**
- `FUENTE_DINAMICA_DAO_TYPE_DEV` / `FUENTE_DINAMICA_DAO_TYPE_PROD`
- `COUCHDB_URL_DEV` / `COUCHDB_URL_PROD`
- `COUCHDB_USER_DEV` / `COUCHDB_USER_PROD`
- `COUCHDB_PASSWORD_DEV` / `COUCHDB_PASSWORD_PROD`
- `COUCHDB_DB_DINAMICA_DEV` / `COUCHDB_DB_DINAMICA_PROD`

**Proxy MetaMapa (Dev/Prod):**
- `METAMAPA_PROXY_URL_DEV` / `METAMAPA_PROXY_URL_PROD`

**Proxy Demo (Dev/Prod):**
- `FUENTE_PROXY_DEMO_DAO_TYPE_DEV` / `FUENTE_PROXY_DEMO_DAO_TYPE_PROD`
- `COUCHDB_DB_PROXY_DEMO_DEV` / `COUCHDB_DB_PROXY_DEMO_PROD`

### Serverless Configuration

File: `apps/fuentes/serverless.yml`

Key features:
- **Single API Gateway** for all fuentes
- **Path-based routing** (`/estatica/*`, `/dinamica/*`, etc.)
- **Individual packaging** (each function has its own JAR)
- **CloudWatch Logs** with 7-day retention
- **Environment-specific configuration** (dev/prod stages)
- **CORS enabled** for frontend integration

### Implementation Details

**Main.java Pattern (all fuentes):**
```java
public static Javalin createApp() {
    // App configuration
    // Conditionally disable OpenAPI/Swagger in Lambda
    if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") == null) {
        // Register OpenAPI, Swagger, ReDoc plugins
    }
    return app;
}

private static void configureRoutes(Javalin app, Controller controller) {
    // Health/info endpoints only in non-Lambda
    if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") == null) {
        app.get("/health", Main::healthCheck);
        app.get("/", Main::infoServicio);
    }
    // Functional endpoints (available in both modes)
    app.get("/hechos", controller::obtenerHechos);
}

public static void main(String[] args) {
    if (System.getenv("AWS_LAMBDA_FUNCTION_NAME") != null) {
        return; // Lambda mode - don't start server
    }
    Javalin app = createApp();
    app.start(PORT);
}
```

**LambdaHandler.java Pattern (all fuentes):**
```java
public class LambdaHandler implements RequestHandler<AwsProxyRequest, AwsProxyResponse> {
    private static Javalin app;

    static {
        // Cold start initialization
        app = Main.createApp();
        app.start(0); // Random available port
    }

    @Override
    public AwsProxyResponse handleRequest(AwsProxyRequest request, Context context) {
        // Forward request to internal Javalin via localhost HTTP
        // Return formatted AwsProxyResponse
    }
}
```

### Monitoring

Lambda functions log to CloudWatch:
```bash
# View logs for specific function
aws logs tail /aws/lambda/metamapa-fuentes-dev-fuente-estatica --follow

# Filter logs
aws logs filter-log-events \
  --log-group-name /aws/lambda/metamapa-fuentes-dev-fuente-estatica \
  --filter-pattern "ERROR"
```

### Cost Optimization

- **Memory:** 1024-1536 MB (based on complexity)
- **Timeout:** 28 seconds (under API Gateway 29s limit)
- **Provisioned concurrency:** Not used (infrequent traffic)
- **Cold start:** ~2-3 seconds (Java 17 + Javalin)

### Troubleshooting

**Issue: JAR not found during deployment**
```bash
# Ensure JARs are built before deploying
mvn clean package -DskipTests
```

**Issue: Environment variables not set**
```bash
# Verify secrets in GitHub repository settings
# Check serverless.yml environment mappings
```

**Issue: CouchDB connection failed**
```bash
# Verify CouchDB is accessible from Lambda
# Check security groups and network configuration
# Ensure COUCHDB_URL uses public Railway URL
```

**Issue: Cold start timeout**
```bash
# Increase timeout in serverless.yml
# Consider reducing dependencies in pom.xml
```

## Observability Strategy (Distributed Tracing & Logging)

### Current State

The MetaMapa system is distributed across multiple platforms:
- **Frontend**: Vercel (Next.js)
- **Backend**: AWS Lambda (Fuentes)
- **Services**: Railway (CouchDB)
- **Monitoring**: Railway and Grafana Cloud (Prometheus, Grafana)

**Problem:** No unified observability across platforms. Each platform has its own logging:
- Vercel logs → Vercel dashboard
- AWS Lambda → CloudWatch
- Railway → Railway logs
- Docker → Railway and Grafana Cloud logs

**No way to:**
- Trace a request end-to-end (Vercel → Lambda → Railway)
- Correlate logs across services
- Identify bottlenecks in distributed calls
- Debug cross-platform issues

### Proposed Solution: OpenTelemetry + Grafana Cloud

**Chosen Strategy:** **OTLP (OpenTelemetry Protocol) + AWS Lambda Extensions**

**Architecture:**
```
┌────────────────────────────────────────────────────────┐
│              Grafana Cloud (Free Tier)                 │
│  - Loki (logs): 50 GB/month                           │
│  - Tempo (traces): 50 GB/month                        │
│  - Prometheus (metrics): 10k series                   │
│  - 14 days retention                                  │
└────────────────────────────────────────────────────────┘
           ▲              ▲              ▲
           │              │              │
     [OTLP Export]  [OTLP Export]  [OTLP Export]
           │              │              │
    ┌──────────┐   ┌──────────┐   ┌──────────┐
    │  Vercel  │   │   AWS    │   │ Railway  │
    │(Next.js) │   │ (Lambda) │   │          │
    └──────────┘   └──────────┘   └──────────┘
                       │
                       └─> Lambda Extensions
                           (handles OTLP buffering)
```

**Why this approach:**
- ✅ **Vendor-neutral**: OTLP works across Vercel, AWS, Railway, Docker
- ✅ **Lambda Extensions**: Non-blocking, automatic flushing, minimal code changes
- ✅ **Single source of truth**: All telemetry data in Grafana Cloud
- ✅ **End-to-end tracing**: See complete request flow across platforms
- ✅ **Cost-effective**: Grafana Cloud free tier sufficient for development
- ✅ **Future-proof**: OpenTelemetry is the industry standard

### Lambda Implementation: AWS Lambda Extensions (CHOSEN APPROACH)

**How it works:**
```
┌─────────────────────────────────────────────────────────┐
│            AWS Lambda Function                          │
│                                                         │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Your Handler (LambdaHandler.java)              │  │
│  │  - Creates spans automatically                   │  │
│  │  - Sends to localhost:4318 (OTLP)              │  │
│  └──────────────────────────────────────────────────┘  │
│                      │                                 │
│                      │ OTLP over localhost              │
│                      ▼                                 │
│  ┌──────────────────────────────────────────────────┐  │
│  │  AWS OTel Lambda Extension (Layer)              │  │
│  │  - Receives spans                                │  │
│  │  - Buffers in memory                            │  │
│  │  - Sends batch to Grafana Tempo (async)        │  │
│  └──────────────────────────────────────────────────┘  │
│                      │                                 │
└──────────────────────┼─────────────────────────────────┘
                       │ HTTPS (non-blocking)
                       ▼
            Grafana Tempo (remote)
```

**Why Lambda Extensions:**
- ✅ **Non-blocking**: Extension runs in parallel, doesn't block handler response
- ✅ **Auto-instrumentation**: Traces HTTP requests automatically
- ✅ **Automatic flushing**: Extension handles buffering and sending
- ✅ **Minimal code changes**: Just configuration, no code rewrite needed
- ✅ **Lower cold start**: ~300ms vs ~500ms with manual integration
- ✅ **AWS recommended**: Official AWS OpenTelemetry layer

**Configuration:**
```yaml
# serverless.yml
functions:
  fuente-estatica:
    layers:
      # Official AWS OTel Lambda Extension
      - arn:aws:lambda:us-east-1:901920570463:layer:aws-otel-java-wrapper-amd64-ver-1-32-0:1
    environment:
      # Enable auto-instrumentation
      AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler

      # OpenTelemetry configuration
      OTEL_SERVICE_NAME: fuente-estatica
      OTEL_EXPORTER_OTLP_ENDPOINT: https://tempo-xxx.grafana.net:443
      OTEL_EXPORTER_OTLP_HEADERS: "Authorization=Bearer ${env:GRAFANA_API_KEY}"
      OTEL_TRACES_SAMPLER: always_on
      OTEL_PROPAGATORS: tracecontext,baggage
```

**Code (optional custom spans):**
```java
// Your existing code works as-is with auto-instrumentation
// Add custom spans only where needed:
Span span = GlobalOpenTelemetry.getTracer("fuente-estatica")
    .spanBuilder("custom-operation")
    .startSpan();
try (Scope scope = span.makeCurrent()) {
    // Your logic
} finally {
    span.end(); // No flush needed - extension handles it
}
```

### Alternative Approaches (Not Chosen)

#### Manual OpenTelemetry Integration
❌ **Not chosen** - Requires more code, blocks handler on flush, higher cold start overhead.

#### AWS X-Ray + OpenTelemetry Collector
❌ **Not chosen** - Requires extra infrastructure (OpenTelemetry Collector), more complexity, vendor lock-in to AWS.

### Implementation Roadmap

**Phase 1: Setup Grafana Cloud (5 minutes)**
1. Create free account at grafana.com
2. Obtain credentials:
   - Tempo endpoint: `tempo-xxx.grafana.net:443`
   - API Key: `glc_xxx...`
3. Add to GitHub Secrets:
   - `OTEL_EXPORTER_OTLP_ENDPOINT`
   - `GRAFANA_API_KEY`

**Phase 2: Instrument Normalizador (TypeScript - easiest)**
```bash
cd apps/normalizador
npm install @opentelemetry/sdk-node \
            @opentelemetry/auto-instrumentations-node \
            @opentelemetry/exporter-trace-otlp-http
```

```typescript
// src/tracing.ts
import { NodeSDK } from '@opentelemetry/sdk-node';
import { OTLPTraceExporter } from '@opentelemetry/exporter-trace-otlp-http';
import { getNodeAutoInstrumentations } from '@opentelemetry/auto-instrumentations-node';

const sdk = new NodeSDK({
  serviceName: 'metamapa-normalizador',
  traceExporter: new OTLPTraceExporter({
    url: process.env.OTEL_EXPORTER_OTLP_ENDPOINT + '/v1/traces',
    headers: {
      'Authorization': `Bearer ${process.env.GRAFANA_API_KEY}`
    }
  }),
  instrumentations: [getNodeAutoInstrumentations()]
});

sdk.start();
```

**Phase 3: Instrument Lambda Functions**

Update `serverless.yml`:
```yaml
provider:
  # ... existing config
  environment:
    OTEL_EXPORTER_OTLP_ENDPOINT: ${env:OTEL_EXPORTER_OTLP_ENDPOINT}
    GRAFANA_API_KEY: ${env:GRAFANA_API_KEY}

functions:
  fuente-estatica:
    layers:
      - arn:aws:lambda:us-east-1:901920570463:layer:aws-otel-java-wrapper-amd64-ver-1-32-0:1
    environment:
      AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler
      OTEL_SERVICE_NAME: fuente-estatica

  fuente-dinamica:
    layers:
      - arn:aws:lambda:us-east-1:901920570463:layer:aws-otel-java-wrapper-amd64-ver-1-32-0:1
    environment:
      AWS_LAMBDA_EXEC_WRAPPER: /opt/otel-handler
      OTEL_SERVICE_NAME: fuente-dinamica

  # ... repeat for other functions
```

**Phase 4: Instrument Frontend (Vercel)**
```bash
cd apps/ui-admin
npm install @vercel/otel
```

```typescript
// instrumentation.ts (Next.js 15)
import { registerOTel } from '@vercel/otel'

export function register() {
  registerOTel({
    serviceName: 'metamapa-frontend',
    traceExporter: {
      url: process.env.OTEL_EXPORTER_OTLP_ENDPOINT,
      headers: {
        'Authorization': `Bearer ${process.env.GRAFANA_CLOUD_API_KEY}`
      }
    }
  })
}
```

Add to Vercel environment variables:
- `OTEL_EXPORTER_OTLP_ENDPOINT`
- `GRAFANA_CLOUD_API_KEY`

**Phase 5: View Traces in Grafana**
1. Navigate to Grafana → Explore
2. Select Tempo datasource
3. Query traces by service, operation, or trace ID
4. View service map showing all services
5. Drill down into specific traces to see:
   - Request flow across services
   - Latency breakdown
   - Error details
   - Correlated logs (if using Loki)

### Expected Results

**End-to-End Trace Example:**
```
Trace ID: abc123xyz                          Total: 1250ms
├─ Vercel (Next.js)                          150ms
│  └─ API fetch("/api/hechos")
│
├─ API Gateway                               50ms
│  └─ Route to Lambda
│
├─ Lambda: fuente-dinamica                   800ms
│  ├─ Handler execution                      100ms
│  ├─ CouchDB query (Railway)                500ms
│  └─ Normalizador call                      200ms
│
└─ Lambda: normalizador (Railway)            250ms
   ├─ Category normalization                 100ms
   ├─ Date normalization                     50ms
   └─ Coordinate validation                  100ms
```

**Insights gained:**
- Identify bottlenecks (e.g., CouchDB query is slow)
- Measure cross-platform latency
- Debug distributed errors
- Optimize based on real data

### Cost Considerations

**Grafana Cloud Free Tier:**
- 50 GB logs/month (Loki)
- 50 GB traces/month (Tempo)
- 10k active series (Prometheus)
- 14 days retention
- **Sufficient for development and small production loads**

**If exceeding free tier:**
- Loki: $0.50/GB
- Tempo: $0.30/GB
- Typically costs $10-30/month for moderate traffic

**Alternative:** Self-host Loki + Tempo (free, but requires infrastructure)

### Status

⏳ **Not yet implemented** - Strategy documented for future implementation.

**Priority:** Medium - Current CloudWatch + docker-compose logs are functional but lack cross-platform correlation.

**Effort:** ~4-6 hours total implementation time.

## Development Notes

- All Java microservices use **Javalin** framework
- Maven Shade plugin creates **fat JARs** for deployment
- Each service has its own **Dockerfile** for containerization
- **Shared domain logic** prevents code duplication across services
- Alert integration sends HTTP POST to proxy services for consensus handling
- **Normalizador** is the only TypeScript service (serverless architecture)
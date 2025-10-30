# MetaMapa

Sistema de microservicios para la gestión y agregación de datos de diversas fuentes con capacidades de monitoreo.

## Arquitectura

### Microservicios

El sistema está compuesto por 6 microservicios independientes:

1. **Fuentes Estáticas** (Puerto 7001)
   - Gestión de datos estáticos desde archivos CSV
   - Almacenamiento en MinIO/S3
   - Independiente de bases de datos

2. **Fuentes Dinámicas** (Puerto 7002)
   - Procesamiento de datos dinámicos
   - Integración con CouchDB para persistencia NoSQL
   - Almacenamiento flexible de documentos

3. **Proxy MetaMapa** (Puerto 7003)
   - Proxy hacia fuentes externas de MetaMapa
   - Integración con APIs externas
   - Independiente de bases de datos

4. **Proxy Demo** (Puerto 7004)
   - Proxy de demostración para testing
   - Integración con CouchDB para persistencia
   - Almacenamiento de datos de prueba

5. **Agregador** (Puerto 7005)
   - Agregación de datos de múltiples fuentes
   - Persistencia con Hibernate/PostgreSQL
   - Procesamiento y consolidación de datos

6. **MetaMapa Administrativo** (Puerto 7006)
   - API administrativa para gestión de colecciones
   - API pública para consultas
   - Persistencia con Hibernate/PostgreSQL
   - Gestión de solicitudes de eliminación

7. **UI Admin** (Puerto 3000)
   - Interfaz web de administración con Next.js 15
   - Visualización de hechos en mapa interactivo (Mapbox)
   - Sistema de filtrado avanzado con sincronización de URL
   - Gestión de colecciones y solicitudes de eliminación
   - Autenticación con Clerk.js

### Bases de Datos

- **PostgreSQL**: Base de datos relacional principal para Agregador y MetaMapa
- **CouchDB**: Base de datos NoSQL para Fuentes Dinámicas y Proxy Demo
- **MinIO**: Almacenamiento S3-compatible para archivos y datos estáticos

### Stack de Monitoreo

- **Prometheus** (Puerto 9090): Recolección de métricas
- **Alertmanager** (Puerto 9093): Gestión de alertas
- **Grafana** (Puerto 3000): Dashboards y visualización

## Estructura del Proyecto

```
MetaMapa/
├── pom.xml                          # POM padre principal
├── docker-compose.yml               # Orquestación de contenedores
├── build-and-dockerize.sh           # Script de build y dockerización
├── .env.example                     # Variables de entorno de ejemplo
├── apps/
│   ├── shared/                      # Módulo de dominio compartido
│   │   ├── pom.xml
│   │   └── src/main/java/utn/dds/
│   │       ├── dominio/             # Entidades de dominio
│   │       ├── dto/                 # Data Transfer Objects
│   │       ├── jpa/                 # Entidades JPA
│   │       └── mappers/             # Mappers entre dominio y JPA
│   ├── fuentes/                     # Microservicios de fuentes de datos
│   │   ├── estatica/                # Fuentes de datos estáticas
│   │   ├── dinamica/                # Fuentes de datos dinámicas
│   │   └── proxy/                   # Servicios proxy
│   │       ├── metamapa/            # Proxy MetaMapa
│   │       └── demo/                # Proxy Demo
│   ├── agregador/                   # Microservicio agregador
│   ├── metamapa/                    # Microservicio principal MetaMapa
│   ├── ui-admin/                    # Interfaz de administración (Next.js)
│   │   ├── app/                     # Pages (Next.js App Router)
│   │   ├── components/              # Componentes React
│   │   ├── lib/                     # API client y utilidades
│   │   ├── types/                   # Definiciones TypeScript
│   │   └── hooks/                   # Custom React hooks
│   ├── scheduler/                   # Servicio de programación de tareas
│   ├── normalizador/                # Servicio de normalización
│   └── estadisticas/                # Stack de monitoreo
│       ├── metric-collector/        # Configuración de Prometheus
│       ├── gestor-alertas/          # Configuración de Alertmanager
│       └── grafana/                 # Configuración de Grafana
```

## APIs Principales

### API Pública (Puerto 7006)
- `GET /api/hechos` - Obtener hechos con filtros y paginación
  - Filtros: `categoria`, `titulo`, `descripcion`, `origen`, `estado`, `fechaAcontecimiento`, `latitud`, `longitud`, `etiquetas`
  - Paginación: `pagina` (0-indexed), `tamanioPagina` (default: 10, max: 100)
- `POST /api/hechos/{uuid}/reportar` - Reportar un hecho
- `GET /api/colecciones` - Listar colecciones con paginación
- `GET /api/colecciones/{identificador}` - Obtener colección por identificador
- `GET /api/colecciones/{identificador}/hechos` - Obtener hechos de una colección
- `POST /api/solicitudes` - Crear solicitud de eliminación

### API Administrativa (Puerto 7006)
- `GET /administrador/colecciones` - Listar colecciones (admin)
- `POST /administrador/coleccion` - Crear nueva colección
- `PUT /administrador/coleccion/{id}` - Actualizar colección
- `DELETE /administrador/coleccion/{id}` - Eliminar colección
- `GET /administrador/solicitudes` - Listar solicitudes de eliminación
- `PUT /administrador/solicitud/{uuid}/aceptar` - Aceptar solicitud
- `PUT /administrador/solicitud/{uuid}/rechazar` - Rechazar solicitud

## Configuración del Entorno

### 1. Crear archivo de configuración

Copia el archivo de ejemplo y personalízalo:

```bash
cp .env.example .env
```

### 2. Configurar variables de entorno

Edita el archivo `.env` con tus configuraciones:

```bash
# Base de datos PostgreSQL
POSTGRES_DB=metamapa_db
POSTGRES_USER=metamapa
POSTGRES_PASSWORD=metamapa123

# pgAdmin
PGADMIN_EMAIL=admin@metamapa.com
PGADMIN_PASSWORD=admin123

# CouchDB Configuration
COUCHDB_USER=admin
COUCHDB_PASSWORD=admin123

# MinIO S3 Configuration
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=minioadmin123

# S3 Configuration for Services
S3_ENDPOINT=http://minio:9000
S3_BUCKET=metamapa-data
S3_REGION=us-east-1

# Configuración de DAOs
AGREGADOR_DAO_TYPE=hibernate
METAMAPA_DAO_TYPE=hibernate
FUENTE_ESTATICA_DAO_TYPE=s3
FUENTE_DINAMICA_DAO_TYPE=filesystem
FUENTE_PROXY_DEMO_DAO_TYPE=filesystem
```

## Instalación y Ejecución

### Prerrequisitos

- Docker y Docker Compose
- Java 17
- Maven 3.8+

### Opción 1: Con Docker Compose (Recomendado)

1. **Preparar el entorno:**
   ```bash
   cp .env.example .env
   # Editar .env con tus configuraciones
   ```

2. **Construir y ejecutar todos los servicios:**
   ```bash
   ./build-and-dockerize.sh
   docker-compose up -d
   ```

3. **Verificar servicios:**
   ```bash
   docker-compose ps
   ```

### Opción 2: Desarrollo Local

1. **Compilar todo el proyecto:**
   ```bash
   mvn clean compile
   ```

2. **Ejecutar servicios específicos:**
   ```bash
   # Agregador
   cd apps/agregador
   mvn exec:java -Dexec.mainClass="utn.dds.agregador.Main"

   # MetaMapa
   cd apps/metamapa
   mvn exec:java -Dexec.mainClass="utn.dds.metamapa.Main"
   ```

3. **Ejecutar UI Admin (Next.js):**
   ```bash
   cd apps/ui-admin

   # Instalar dependencias (primera vez)
   yarn install

   # Configurar variables de entorno
   cp .env.example .env.local
   # Editar .env.local con tus credenciales

   # Ejecutar servidor de desarrollo
   yarn dev
   ```

   La aplicación estará disponible en http://localhost:3000

4. **Crear JARs ejecutables:**
   ```bash
   mvn clean package
   ```

## Acceso a Servicios

### Microservicios
- **Fuentes Estáticas**: http://localhost:7001
- **Fuentes Dinámicas**: http://localhost:7002
- **Proxy MetaMapa**: http://localhost:7003
- **Proxy Demo**: http://localhost:7004
- **Agregador**: http://localhost:7005
- **MetaMapa**: http://localhost:7006

### Interfaz Web
- **UI Admin**: http://localhost:3000
  - `/hechos` - Vista de hechos con mapa y filtros
  - `/colecciones` - Listado de colecciones
  - `/solicitudes` - Solicitudes de eliminación
  - `/admin` - Dashboard administrativo (requiere autenticación)

### Bases de Datos
- **PostgreSQL**: localhost:5432
- **CouchDB**: http://localhost:5984
- **CouchDB Web UI**: http://localhost:5984/_utils
- **MinIO**: http://localhost:9000
- **MinIO Console**: http://localhost:9001

### Herramientas de Administración
- **pgAdmin**: http://localhost:5050
- **Prometheus**: http://localhost:9090
- **Alertmanager**: http://localhost:9093
- **Grafana**: http://localhost:3000 (admin/admin)

### APIs y Documentación
- **API MetaMapa**: http://localhost:7006
- **Swagger UI**: http://localhost:7006/swagger-ui
- **API Documentation**: http://localhost:7006/redoc

## Monitoreo y Métricas

### Configuración de Alertas

Las alertas de Prometheus se envían automáticamente al endpoint:
```
http://proxy-metamapa:7003/consenso
```

### Integración de Alertas en Código

```java
// Ejemplo en proxy-metamapa
app.post("/consenso", ctx -> {
    String alertPayload = ctx.body();
    // Procesar alerta recibida
    logger.info("Alerta recibida: {}", alertPayload);
    ctx.status(200);
});
```

### Métricas de Microservicios

Cada microservicio debe exponer métricas en:
```
http://localhost:PORT/metrics
```

## Desarrollo

### Estructura de Dominio Compartido

El módulo `apps/shared` contiene:
- **Entidades de dominio**: `Hecho`, `Contribuyente`, `Coleccion`
- **Enums**: `EstadoHecho`, `TipoHecho`, `Origen`
- **Strategy patterns**: Para categorización y criterios
- **DTOs**: Para transferencia de datos
- **Mappers**: Entre dominio y JPA

### Agregando Nuevos Endpoints

1. **Definir en el controlador:**
   ```java
   @OpenApi(/* documentación */)
   public void nuevoEndpoint(Context ctx) {
       // lógica del endpoint
   }
   ```

2. **Registrar en routes:**
   ```java
   app.get("/nuevo-endpoint", controller::nuevoEndpoint);
   ```

3. **Implementar lógica en service y repository**

### Testing

```bash
# Ejecutar todos los tests
mvn test

# Tests de un módulo específico
cd apps/metamapa
mvn test
```

## Tecnologías

### Backend
- **Java 17**
- **Maven** (multi-module project)
- **Javalin** (web framework)
- **Hibernate** (ORM)
- **Jackson** (JSON)
- **SLF4J** (logging)
- **OpenCSV** (CSV processing)

### Frontend (UI Admin)
- **Next.js 15** (React 19)
- **TypeScript**
- **HeroUI** (component library)
- **Tailwind CSS v4** (styling)
- **Mapbox GL** (map visualization)
- **Clerk.js** (authentication)
- **React Hook Form** + **Zod** (forms & validation)
- **Framer Motion** (animations)
- **Recharts** (data visualization)

### Bases de Datos
- **PostgreSQL 15** (relacional)
- **CouchDB 3.3** (NoSQL)
- **MinIO** (S3-compatible storage)

### Infraestructura
- **Docker & Docker Compose**
- **Prometheus** (métricas)
- **Grafana** (visualización)
- **Alertmanager** (alertas)

### APIs y Documentación
- **OpenAPI 3** (documentación)
- **Swagger UI** (interfaz interactiva)
- **ReDoc** (documentación estática)

## Troubleshooting

### Problemas Comunes

1. **Puerto ocupado:**
   ```bash
   # Verificar puertos en uso
   docker-compose ps
   lsof -i :7006
   ```

2. **Base de datos no conecta:**
   ```bash
   # Verificar logs
   docker-compose logs postgres
   docker-compose logs couchdb
   ```

3. **Rebuild necesario:**
   ```bash
   docker-compose down
   ./build-and-dockerize.sh
   docker-compose up -d
   ```

### Logs y Debug

```bash
# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f metamapa

# Ver logs en tiempo real
docker-compose logs -f --tail=100
```

## Contribución

1. Crear feature branch desde `main`
2. Implementar cambios con tests
3. Actualizar documentación si es necesario
4. Crear Pull Request con descripción detallada

### Convenciones de Código

- Usar Java 17 features
- Seguir patrones existentes en el codebase
- Documentar APIs con OpenAPI
- Agregar tests para nuevas funcionalidades
- Mantener la separación de responsabilidades entre capas
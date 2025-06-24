# MetaMapa

Proyecto de microservicios con dominio compartido para el sistema MetaMapa.

## Estructura del Proyecto

```
MetaMapa/
├── pom.xml                          # POM padre principal
├── docker-compose.yml               # Orquestación de contenedores
├── build-and-dockerize.sh           # Script de build y dockerización
├── apps/
│   ├── dominio/                     # Módulo de dominio compartido
│   │   ├── pom.xml
│   │   └── src/main/java/utn/dds/model/
│   │       ├── *.java               # Clases de dominio principales
│   │       ├── criterios/           # Estrategias de criterios
│   │       └── fuentes/             # Modelos de fuentes de datos
│   │           ├── estatica/        # Fuentes estáticas
│   │           │   └── strategies/  # Estrategias de procesamiento
│   │           └── proxy/           # Fuentes proxy
│   └── fuentes/                     # Microservicios de fuentes de datos
│       ├── estatica/                # Fuentes de datos estáticas
│       │   ├── pom.xml
│       │   ├── Dockerfile
│       │   └── src/main/java/utn/dds/fuentes/estatica/
│       ├── dinamica/                # Fuentes de datos dinámicas
│       │   ├── pom.xml
│       │   ├── Dockerfile
│       │   └── src/main/java/utn/dds/fuentes/dinamica/
│       └── proxy/                   # Servicios proxy
│           ├── metamapa/            # Proxy para MetaMapa
│           │   ├── pom.xml
│           │   ├── Dockerfile
│           │   └── src/main/java/utn/dds/fuentes/proxy/metamapa/
│           └── demo/                # Proxy demo
│               ├── pom.xml
│               ├── Dockerfile
│               └── src/main/java/utn/dds/fuentes/proxy/demo/
```

## Módulos

### Dominio (`apps/dominio`)
Contiene las clases de dominio compartidas entre todos los microservicios. Este módulo incluye:

**Clases principales:**
- `Hecho` - Entidad principal del dominio
- `Contribuyente` - Usuario que aporta hechos
- `Coleccion` - Agrupación de hechos con criterios
- `SolicitudEliminacion` - Solicitudes de eliminación de hechos

**Enums:**
- `Origen` - Origen del hecho (DATASET, MANUAL, CONTRIBUYENTE)
- `TipoHecho` - Tipo de hecho (TEXTO, MULTIMEDIA)
- `EstadoHecho` - Estado del hecho (ACTIVO, OCULTO)
- `EstadoSolicitud` - Estado de solicitud (ACTIVO, OCULTO)
- `TipoFuente` - Tipo de fuente (ESTATICA, DINAMICA, PROXY)

**Criterios de búsqueda:**
- `HechoStrategy` - Interfaz para estrategias de filtrado
- `CategoriaStrategy` - Filtro por categoría
- `TituloStrategy` - Filtro por título

**Fuentes de datos:**
- `FuenteDeDatos` - Interfaz base para fuentes
- `FuenteEstatica` - Fuentes de datos estáticas (CSV, etc.)
- `FuenteDinamica` - Fuentes de datos dinámicas
- `FuenteMetaMapa`, `FuenteDemo`, `FuenteProxyDemo` - Fuentes proxy
- `CSVStrategy` - Procesador de archivos CSV
- `Conexion` - Interfaz para conexiones externas

**Utilidades:**
- `DetectorSpam` - Interfaz para detección de spam
- `AlgoritmoTFIDF` - Implementación de detección de spam

### Fuentes Estáticas (`apps/fuentes/estatica`)
Microservicio que maneja fuentes de datos estáticas (archivos CSV, JSON, etc.).
- **Puerto**: 7001
- **Endpoints**:
  - `GET /health` - Health check
  - `GET /` - Información del servicio
  - `GET /datos` - Obtener datos estáticos

### Fuentes Dinámicas (`apps/fuentes/dinamica`)
Microservicio que maneja fuentes de datos dinámicas (APIs externas, bases de datos, etc.).
- **Puerto**: 7002
- **Endpoints**:
  - `GET /health` - Health check
  - `GET /` - Información del servicio
  - `GET /datos` - Obtener datos dinámicos

### Proxy MetaMapa (`apps/fuentes/proxy/metamapa`)
Servicio proxy para la integración con MetaMapa.
- **Puerto**: 7003
- **Endpoints**:
  - `GET /health` - Health check
  - `GET /` - Información del servicio
  - `GET /datos` - Obtener datos del proxy

### Proxy Demo (`apps/fuentes/proxy/demo`)
Servicio proxy demo para pruebas y desarrollo.
- **Puerto**: 7004
- **Endpoints**:
  - `GET /health` - Health check
  - `GET /` - Información del servicio
  - `GET /datos` - Obtener datos del proxy demo

## Tecnologías

- **Java 17**
- **Maven** - Gestión de dependencias y build
- **Javalin** - Framework web para microservicios
- **SLF4J** - Logging
- **Jackson** - Serialización JSON
- **JUnit 5** - Testing
- **Docker** - Containerización
- **Docker Compose** - Orquestación de contenedores
- **OpenCSV** - Procesamiento de archivos CSV

## Compilación y Ejecución

### Compilar todo el proyecto
```bash
mvn clean compile
```

### Ejecutar un microservicio específico
```bash
# Fuentes estáticas
cd apps/fuentes/estatica
mvn exec:java -Dexec.mainClass="utn.dds.fuentes.estatica.Main"

# Fuentes dinámicas
cd apps/fuentes/dinamica
mvn exec:java -Dexec.mainClass="utn.dds.fuentes.dinamica.Main"

# Proxy MetaMapa
cd apps/fuentes/proxy/metamapa
mvn exec:java -Dexec.mainClass="utn.dds.fuentes.proxy.metamapa.Main"

# Proxy Demo
cd apps/fuentes/proxy/demo
mvn exec:java -Dexec.mainClass="utn.dds.fuentes.proxy.demo.Main"
```

### Crear JARs ejecutables
```bash
mvn clean package
```

Los JARs se generarán en el directorio `target/` de cada módulo.

## Docker y Containerización

### Construir y Dockerizar (Recomendado)
```bash
# Construir JARs y crear imágenes Docker
./build-and-dockerize.sh
```

### Construir imágenes Docker manualmente
```bash
# Primero compilar el proyecto
mvn clean package -DskipTests

# Construir cada imagen
docker build -t metamapa/fuentes-estatica:latest ./apps/fuentes/estatica/
docker build -t metamapa/fuentes-dinamica:latest ./apps/fuentes/dinamica/
docker build -t metamapa/proxy-metamapa:latest ./apps/fuentes/proxy/metamapa/
docker build -t metamapa/proxy-demo:latest ./apps/fuentes/proxy/demo/
```

### Ejecutar con Docker Compose
```bash
# Levantar todos los servicios
docker-compose up -d

# Ver logs de todos los servicios
docker-compose logs -f

# Ver logs de un servicio específico
docker-compose logs -f fuentes-estatica

# Detener todos los servicios
docker-compose down
```

### Ejecutar contenedores individuales
```bash
# Fuentes estáticas
docker run -p 7001:7001 metamapa/fuentes-estatica:latest

# Fuentes dinámicas
docker run -p 7002:7002 metamapa/fuentes-dinamica:latest

# Proxy MetaMapa
docker run -p 7003:7003 metamapa/proxy-metamapa:latest

# Proxy Demo
docker run -p 7004:7004 metamapa/proxy-demo:latest
```

### Verificar servicios
Una vez ejecutados, puedes verificar que los servicios estén funcionando:

```bash
# Health checks
curl http://localhost:7001/health
curl http://localhost:7002/health
curl http://localhost:7003/health
curl http://localhost:7004/health

# Información de servicios
curl http://localhost:7001/
curl http://localhost:7002/
curl http://localhost:7003/
curl http://localhost:7004/
```

## Desarrollo

1. **Modelos de dominio** ya están migrados en `apps/dominio/src/main/java/utn/dds/model/`
2. Implementar la lógica específica en cada microservicio
3. Agregar endpoints según las necesidades de cada servicio
4. Configurar las dependencias necesarias en cada `pom.xml`
5. Actualizar Dockerfiles si se agregan nuevas dependencias o configuraciones

## Notas

- Todos los microservicios usan Javalin como framework web
- Cada servicio tiene su propio puerto para evitar conflictos
- El módulo de dominio es compartido entre todos los servicios
- Se incluye logging configurado con SLF4J
- Los JARs son ejecutables con el plugin shade de Maven
- Cada microservicio está containerizado con Docker
- Docker Compose facilita el despliegue de todos los servicios
- Las imágenes Docker usan OpenJDK 17 slim para optimizar el tamaño
- **23 clases de dominio** migradas exitosamente al módulo compartido
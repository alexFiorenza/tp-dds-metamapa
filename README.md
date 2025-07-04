# MetaMapa

Proyecto de microservicios con dominio compartido para el sistema MetaMapa.

## Estructura del Proyecto

```
MetaMapa/
├── pom.xml                          # POM padre principal
├── docker-compose.yml               # Orquestación de contenedores
├── build-and-dockerize.sh           # Script de build y dockerización
├── apps/
│   ├── shared/                      # Módulo de dominio compartido
│   │   ├── pom.xml
│   │   └── src/main/java/utn/dds/dominio/
│   ├── fuentes/                     # Microservicios de fuentes de datos
│   │   ├── estatica/                # Fuentes de datos estáticas
│   │   ├── dinamica/                # Fuentes de datos dinámicas
│   │   └── proxy/                   # Servicios proxy
│   ├── agregador/                   # Microservicio agregador
│   │   ├── pom.xml
│   │   └── src/main/java/utn/dds/agregador/
│   ├── metamapa/                    # Microservicio principal MetaMapa
│   └── estadisticas/                # Stack de monitoreo y dashboards
│       ├── prometheus/              # Configuración de Prometheus
│       │   └── prometheus.yml
│       ├── alertmanager/            # Configuración de Alertmanager
│       │   └── config.yml
│       └── grafana/                 # Configuración de Grafana
│           ├── provisioning/
│           │   ├── datasources/datasource.yml
│           │   └── dashboards/dashboard.yml
│           └── dashboards/          # Dashboards JSON personalizados
```

## Módulos y Microservicios

### Dominio Compartido (`apps/shared`)
Contiene las clases de dominio, enums, fuentes, criterios y repositorios abstractos.

### Fuentes Estáticas (`apps/fuentes/estatica`)
Microservicio que maneja fuentes de datos estáticas (archivos CSV, JSON, etc.).
- **Puerto:** 7001
- **Endpoints:**
  - `GET /health` - Health check
  - `GET /` - Información del servicio

### Fuentes Dinámicas (`apps/fuentes/dinamica`)
Microservicio que maneja fuentes de datos dinámicas (APIs externas, bases de datos, etc.).
- **Puerto:** 7002
- **Endpoints:**
  - `GET /health` - Health check
  - `GET /` - Información del servicio

### Proxy MetaMapa (`apps/fuentes/proxy/metamapa`)
Servicio proxy para la integración con MetaMapa.
- **Puerto:** 7003
- **Endpoints:**
  - `GET /health` - Health check
  - `GET /` - Información del servicio

### Proxy Demo (`apps/fuentes/proxy/demo`)
Servicio proxy demo para pruebas y desarrollo.
- **Puerto:** 7004
- **Endpoints:**
  - `GET /health` - Health check
  - `GET /` - Información del servicio

### Agregador (`apps/agregador`)
Microservicio que agrega y expone datos de múltiples fuentes.
- **Puerto:** 7005
- **Endpoints:**
  - `GET /health` - Health check
  - `GET /` - Información del servicio

### MetaMapa (`apps/metamapa`)
Microservicio principal del sistema MetaMapa.
- **Puerto:** 7006
- **Endpoints:**
  - `GET /health` - Health check
  - `GET /` - Información del servicio

## Estadísticas y Monitoreo

### apps/estadisticas
Contiene la configuración para el stack de monitoreo y visualización:

- **Prometheus:**
  - Scrapea métricas de los microservicios en `/metrics`.
  - Configuración en `apps/estadisticas/prometheus/prometheus.yml`.
- **Alertmanager:**
  - Gestiona alertas y notificaciones.
  - Configuración en `apps/estadisticas/alertmanager/config.yml`.
  - **Integración:** Envía alertas vía HTTP POST al endpoint `/consenso` del microservicio `proxy-metamapa`.
- **Grafana:**
  - Dashboards y visualización de métricas.
  - Provisioning automático de datasources y dashboards en `apps/estadisticas/grafana/provisioning/`.

### Ejemplo de integración de alertas
Cuando se dispara una alerta en Prometheus, Alertmanager enviará un POST a:
```
http://proxy-metamapa:7003/consenso
```
con el payload de la alerta.

Puedes implementar el handler en `proxy-metamapa` así:
```java
app.post("/consenso", ctx -> {
    String payload = ctx.body();
    // Procesar alerta recibida
    ctx.status(200);
});
```

## Tecnologías

- **Java 17**
- **Maven**
- **Javalin**
- **SLF4J**
- **Jackson**
- **JUnit 5**
- **Docker**
- **Docker Compose**
- **OpenCSV**

## Compilación y Ejecución

### Compilar todo el proyecto
```bash
mvn clean compile
```

### Ejecutar un microservicio específico
```bash
# Ejemplo para el agregador
cd apps/agregador
mvn exec:java -Dexec.mainClass="utn.dds.agregador.Main"

# Ejemplo para MetaMapa
cd apps/metamapa
mvn exec:java -Dexec.mainClass="utn.dds.metamapa.Main"
```

### Crear JARs ejecutables
```bash
mvn clean package
```

## Docker y Containerización

### Construir y Dockerizar
```bash
./build-and-dockerize.sh
```

### Ejecutar con Docker Compose
```bash
docker-compose up -d
```

### Verificar servicios
```bash
curl http://localhost:7005/health   # Agregador
curl http://localhost:7006/health   # MetaMapa
```

## Desarrollo

1. Modelos de dominio en `apps/shared/src/main/java/utn/dds/dominio/`
2. Implementar lógica específica en cada microservicio
3. Agregar endpoints según necesidades
4. Configurar dependencias necesarias en cada `pom.xml`
5. Actualizar Dockerfiles si se agregan nuevas dependencias

## Notas

- Todos los microservicios usan Javalin
- Cada servicio tiene su propio puerto
- El módulo `shared` es compartido entre todos los servicios
- Los JARs son ejecutables con el plugin shade de Maven
- Docker Compose facilita el despliegue de todos los servicios
- El stack de monitoreo (Prometheus, Alertmanager, Grafana) está listo para usarse desde `apps/estadisticas`.
- Las alertas pueden ser visualizadas en Grafana y gestionadas por Alertmanager.
- Las métricas deben ser expuestas en `/metrics` por cada microservicio para ser recolectadas.
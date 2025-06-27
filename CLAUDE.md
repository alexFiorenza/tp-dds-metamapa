# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

MetaMapa is a microservices-based Java project with shared domain logic. It implements a system for managing and aggregating data from various sources with monitoring capabilities.

### Architecture

- **Multi-module Maven project** with shared domain
- **Microservices architecture** with 6 independent services
- **Javalin-based REST APIs** for each microservice
- **Docker containerization** with compose orchestration
- **Monitoring stack** (Prometheus, Grafana, Alertmanager)

### Key Technologies

- Java 17
- Maven (multi-module project)
- Javalin web framework
- Docker & Docker Compose
- Prometheus, Grafana, Alertmanager for monitoring

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

### Testing
```bash
# Run all tests
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
- Prometheus: 9090
- Alertmanager: 9093
- Grafana: 3000

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
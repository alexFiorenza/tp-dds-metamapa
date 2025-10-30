# MetaMapa Docs - Visualizador de Diagramas

Aplicación web para visualizar y editar diagramas Mermaid del proyecto MetaMapa.

## Características

- 📊 Visualización en tiempo real de diagramas Mermaid
- ✏️ Editor integrado con preview automático
- 💾 Persistencia de cambios en archivos `.mmd`
- 🎨 Interfaz moderna con vista dividida
- 🔄 Actualización automática del preview al editar

## Instalación

```bash
cd apps/docs
npm install
```

## Uso

### Modo desarrollo

```bash
npm run dev
```

Abre http://localhost:8080 en tu navegador.

### Modo producción

```bash
npm start
```

## Estructura

```
apps/docs/
├── diagramas/          # Archivos .mmd con los diagramas
│   ├── arquitectura.mmd
│   ├── clases-dominio.mmd
│   ├── flujo-agregacion.mmd
│   └── monitoreo.mmd
├── public/
│   └── index.html     # Aplicación web
├── server.js          # Servidor Express
├── package.json
└── README.md
```

## API Endpoints

- `GET /api/diagramas` - Lista todos los diagramas disponibles
- `GET /api/diagramas/:nombre` - Obtiene el contenido de un diagrama
- `POST /api/diagramas/:nombre` - Guarda cambios en un diagrama

## Agregar nuevos diagramas

1. Crea un archivo `.mmd` en la carpeta `diagramas/`
2. Escribe tu diagrama usando sintaxis Mermaid
3. Recarga la página web y aparecerá en la lista

## Sintaxis Mermaid

Consulta la documentación oficial: https://mermaid.js.org/

### Tipos de diagramas soportados

- Flowchart (graph)
- Sequence diagram
- Class diagram
- State diagram
- Entity Relationship diagram
- Gantt chart
- Pie chart
- Y más...

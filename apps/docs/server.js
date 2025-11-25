const express = require('express');
const fs = require('fs').promises;
const path = require('path');
const cors = require('cors');

const app = express();
const PORT = process.env.PORT || 8080;
const DIAGRAMAS_DIR = path.join(__dirname, 'diagramas');

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

// Endpoint para listar todos los diagramas
app.get('/api/diagramas', async (req, res) => {
  try {
    const files = await fs.readdir(DIAGRAMAS_DIR);
    const diagramas = files
      .filter(file => file.endsWith('.mmd') || file.endsWith('.md'))
      .map(file => ({
        id: file,
        nombre: file.replace(/\.(mmd|md)$/, ''),
        ruta: file
      }));

    res.json(diagramas);
  } catch (error) {
    console.error('Error al leer diagramas:', error);
    res.status(500).json({ error: 'Error al leer diagramas' });
  }
});

// Endpoint para obtener el contenido de un diagrama específico
app.get('/api/diagramas/:nombre', async (req, res) => {
  try {
    const { nombre } = req.params;
    const filePath = path.join(DIAGRAMAS_DIR, nombre);

    // Verificar que el archivo existe y está dentro del directorio permitido
    const realPath = await fs.realpath(filePath);
    if (!realPath.startsWith(DIAGRAMAS_DIR)) {
      return res.status(403).json({ error: 'Acceso denegado' });
    }

    const contenido = await fs.readFile(filePath, 'utf-8');
    res.json({ contenido, nombre });
  } catch (error) {
    console.error('Error al leer diagrama:', error);
    res.status(404).json({ error: 'Diagrama no encontrado' });
  }
});

// Endpoint para guardar/actualizar un diagrama
app.post('/api/diagramas/:nombre', async (req, res) => {
  try {
    const { nombre } = req.params;
    const { contenido } = req.body;

    if (!contenido) {
      return res.status(400).json({ error: 'Contenido requerido' });
    }

    const filePath = path.join(DIAGRAMAS_DIR, nombre);
    await fs.writeFile(filePath, contenido, 'utf-8');

    res.json({ success: true, mensaje: 'Diagrama guardado correctamente' });
  } catch (error) {
    console.error('Error al guardar diagrama:', error);
    res.status(500).json({ error: 'Error al guardar diagrama' });
  }
});

app.listen(PORT, () => {
  console.log(`🚀 Servidor de documentación corriendo en http://localhost:${PORT}`);
  console.log(`📁 Leyendo diagramas desde: ${DIAGRAMAS_DIR}`);
});

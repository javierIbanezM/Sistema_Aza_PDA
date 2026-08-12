# Streams (Node.js)

> Ver [`event-loop.md`](./event-loop.md) para el modelo de I/O no bloqueante sobre el que se construyen los streams, y [`async-patterns.md`](./async-patterns.md) para `for await...of` sobre streams.

## Por qué streams: procesar sin cargar todo en memoria

```typescript
// Evitar con archivos grandes: carga el archivo COMPLETO en memoria antes de procesar
const contenido = await fs.promises.readFile('export-pedidos.csv', 'utf-8');
const lineas = contenido.split('\n');

// Preferir: procesa por chunks, memoria constante independiente del tamaño del archivo
const stream = fs.createReadStream('export-pedidos.csv', { encoding: 'utf-8' });
```

Con un archivo de 10 GB, la primera opción intenta cargar 10 GB en memoria (probable crash por out-of-memory); la segunda procesa en chunks de tamaño constante (típicamente 64KB por defecto), sin importar el tamaño total del archivo.

## Los cuatro tipos de stream

| Tipo | Dirección | Ejemplo |
|---|---|---|
| `Readable` | Fuente de datos | `fs.createReadStream`, respuesta HTTP entrante |
| `Writable` | Destino de datos | `fs.createWriteStream`, respuesta HTTP saliente |
| `Duplex` | Ambas direcciones, independientes | Sockets TCP |
| `Transform` | Duplex que modifica los datos al pasar | `zlib.createGzip()`, parseo CSV |

## Piping: composición de streams

```typescript
import { pipeline } from 'stream/promises';
import { createReadStream, createWriteStream } from 'fs';
import { createGzip } from 'zlib';

await pipeline(
  createReadStream('export-pedidos.csv'),
  createGzip(),
  createWriteStream('export-pedidos.csv.gz')
);
```

`pipeline` (de `stream/promises`) es preferible a encadenar `.pipe()` manualmente — gestiona automáticamente la propagación de errores y el cierre correcto de todos los streams si cualquiera de ellos falla, algo que `.pipe()` encadenado manualmente no garantiza sin código adicional.

## Backpressure: por qué `pipeline`/`.pipe()` y no leer+escribir manual

```typescript
// Antipatrón: ignora el backpressure, puede agotar memoria si el destino es más lento que la fuente
readable.on('data', (chunk) => {
  writable.write(chunk); // no espera a que el buffer de escritura se vacíe
});
```

Cuando un `Writable` no puede procesar datos tan rápido como el `Readable` los produce, su buffer interno crece — sin gestión de backpressure, esto puede consumir memoria sin límite. `pipeline`/`.pipe()` gestionan esto automáticamente (pausan la lectura cuando el buffer de escritura está lleno, la reanudan cuando se libera) — no reimplementar este mecanismo manualmente.

## Transform streams personalizados

```typescript
import { Transform } from 'stream';

class ParsearLineaCsv extends Transform {
  constructor() {
    super({ objectMode: true }); // permite emitir objetos, no solo Buffer/string
  }

  _transform(chunk: Buffer, encoding: string, callback: Function) {
    const lineas = chunk.toString().split('\n').filter(Boolean);
    lineas.forEach(linea => this.push(this.parsearLinea(linea)));
    callback();
  }

  private parsearLinea(linea: string): PedidoCsvRow {
    const [id, clienteId, total] = linea.split(',');
    return { id, clienteId, total: parseFloat(total) };
  }
}
```

Útil para transformar datos en tránsito (parsear un formato, filtrar, enriquecer) sin necesidad de materializar la colección completa en memoria antes de procesarla.

## Streams en Express/Fastify: respuestas grandes

```typescript
app.get('/export/pedidos.csv', (req, res) => {
  res.setHeader('Content-Type', 'text/csv');
  const stream = generarCsvPedidosStream(); // Readable stream generado desde la base de datos
  stream.pipe(res); // envía al cliente a medida que se genera, sin acumular todo en memoria
});
```

## Checklist

- [ ] ¿El procesamiento de archivos/datos grandes usa streams en lugar de cargar todo en memoria?
- [ ] ¿Se usa `pipeline` (no `.pipe()` encadenado manual sin manejo de errores) para componer streams?
- [ ] ¿Se ha evitado reimplementar backpressure manualmente con `.on('data', ...)` + `.write()` directo?

Ver también: [`event-loop.md`](./event-loop.md), [`performance.md`](./performance.md).

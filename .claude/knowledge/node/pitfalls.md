# Pitfalls (Node.js) — Errores frecuentes

> Errores específicos del runtime Node.js/TypeScript. Para trampas de Express/NestJS/Fastify, ver su propia carpeta. Para anti-patrones generales, ver [`../../anti-patterns/`](../../anti-patterns/).

## Bloquear el Event Loop con cálculo síncrono

```typescript
app.get('/reporte', (req, res) => {
  const resultado = calculoRecursivoCostoso(45); // bloquea TODAS las peticiones concurrentes
  res.json(resultado);
});
```

Ver [`event-loop.md`](./event-loop.md) — mover a Worker Threads (ver [`performance.md`](./performance.md)).

## `any` implícito o explícito sin justificar

```typescript
function procesar(datos: any) { /* ... */ } // desactiva la verificación de tipos por completo
```

Ver [`typescript.md`](./typescript.md) — usar `unknown` + type guards en su lugar.

## Fire-and-forget accidental de una Promise

```typescript
notificarPorEmail(pedido); // sin await ni .catch() — unhandled rejection si falla
```

Ver [`async-patterns.md`](./async-patterns.md).

## Cargar un archivo grande completo en memoria

```typescript
const contenido = await fs.promises.readFile('export-grande.csv', 'utf-8'); // riesgo de OOM
```

Ver [`streams.md`](./streams.md) — usar `createReadStream` + `pipeline`.

## Reimplementar backpressure manualmente

```typescript
readable.on('data', (chunk) => writable.write(chunk)); // ignora backpressure, riesgo de OOM
```

Ver [`streams.md`](./streams.md) — usar `pipeline`/`.pipe()`.

## Perder la causa raíz al envolver una excepción

```typescript
try {
  await procesarPago(pedido);
} catch (error) {
  throw new PagoFallidoException('Fallo el pago'); // se pierde el error original
}
```

Ver [`error-handling.md`](./error-handling.md) — usar `{ cause: error }`.

## Uso recursivo descontrolado de `process.nextTick`

```typescript
function bucle() {
  process.nextTick(bucle); // puede inanir el Event Loop, impidiendo que avance a I/O
}
```

Ver [`event-loop.md`](./event-loop.md).

## Checklist de revisión rápida

- [ ] ¿Hay algún cálculo CPU-bound síncrono largo en un handler de petición?
- [ ] ¿Hay algún `any` sin justificar?
- [ ] ¿Alguna `Promise` queda sin manejo de rechazo?
- [ ] ¿Se carga algún archivo grande completo en memoria en lugar de usar streams?

Ver también: [`best-practices.md`](./best-practices.md), [`../../anti-patterns/`](../../anti-patterns/).

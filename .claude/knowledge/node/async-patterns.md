# Async Patterns

> Ver [`event-loop.md`](./event-loop.md) para el mecanismo subyacente. Ver [`../../anti-patterns/callback-hell.md`](../../anti-patterns/callback-hell.md) para el antipatrón que `async/await` resuelve.

## `async/await` como estándar, callbacks solo en APIs legacy

```typescript
// Estándar en este framework
async function crearPedido(clienteId: string, lineas: LineaPedido[]): Promise<Pedido> {
  const cliente = await clienteRepository.buscarPorId(clienteId);
  if (!cliente) throw new ClienteNoEncontradoException(clienteId);

  const pedido = Pedido.crear(clienteId, lineas);
  await pedidoRepository.guardar(pedido);
  return pedido;
}
```

Ver [`../../anti-patterns/callback-hell.md`](../../anti-patterns/callback-hell.md) para el problema que resuelve y el detalle de migración desde callbacks/`.then()` encadenados.

## Paralelizar operaciones independientes

```typescript
// Innecesariamente secuencial
const cliente = await clienteRepository.buscarPorId(clienteId);
const inventario = await inventarioRepository.verificarDisponibilidad(productoId);

// Correcto: en paralelo cuando no hay dependencia real
const [cliente, inventario] = await Promise.all([
  clienteRepository.buscarPorId(clienteId),
  inventarioRepository.verificarDisponibilidad(productoId),
]);
```

## `Promise.allSettled` cuando los fallos individuales no deben abortar todo

```typescript
const resultados = await Promise.allSettled([
  notificarPorEmail(pedido),
  notificarPorSms(pedido),
  actualizarAnalytics(pedido),
]);

resultados.forEach((r, i) => {
  if (r.status === 'rejected') {
    logger.warn(`Notificación ${i} falló`, r.reason); // no aborta las demás
  }
});
```

`Promise.all` aborta ante el primer rechazo, perdiendo el resultado de las promesas que sí tuvieron éxito — `Promise.allSettled` espera a que todas terminen (con éxito o fallo) y expone el resultado individual de cada una, apropiado cuando los fallos son independientes y no deben cancelar el resto del trabajo.

## Manejo de errores: siempre `try/catch` alrededor de `await`, o propagación explícita

```typescript
// En un caso de uso: capturar y traducir a excepción de dominio si aplica
async function confirmarPedido(id: string): Promise<void> {
  try {
    const pedido = await pedidoRepository.buscarPorId(id);
    if (!pedido) throw new PedidoNoEncontradoException(id);
    pedido.confirmar();
    await pedidoRepository.guardar(pedido);
  } catch (error) {
    if (error instanceof PedidoNoEncontradoException) throw error; // ya es del dominio, re-lanzar tal cual
    logger.error('Error inesperado confirmando pedido', error);
    throw new ErrorInternoException('No se pudo confirmar el pedido', { cause: error });
  }
}
```

Ver [`../../standards/exceptions.md`](../../standards/exceptions.md) — nunca dejar una `Promise` rechazada sin capturar en un punto donde el fallo deba traducirse a una excepción de dominio o loguearse con contexto.

## Unhandled Promise Rejections: fallo silencioso en producción

```typescript
// Sin await ni .catch() — si falla, es un "unhandled rejection"
notificarPorEmail(pedido); // fire-and-forget accidental
```

Node.js emite el evento `process.on('unhandledRejection', ...)` ante una Promise rechazada sin manejar — en versiones recientes, esto puede incluso terminar el proceso por defecto. Si una llamada asíncrona genuinamente no necesita esperarse (fire-and-forget intencional), manejar el rechazo explícitamente:

```typescript
notificarPorEmail(pedido).catch(error => logger.error('Fallo al notificar', error));
```

## `for await...of` para iterar streams/async iterables

```typescript
async function procesarArchivoGrande(stream: Readable): Promise<void> {
  for await (const chunk of stream) {
    await procesarChunk(chunk); // procesa secuencialmente, sin cargar todo el archivo en memoria
  }
}
```

Ver [`streams.md`](./streams.md) para el manejo de datos grandes sin agotar memoria.

## Checklist

- [ ] ¿Se ha evitado callback hell (ver [`../../anti-patterns/callback-hell.md`](../../anti-patterns/callback-hell.md)), usando `async/await` de forma consistente?
- [ ] ¿Las operaciones independientes se paralelizan con `Promise.all`/`Promise.allSettled` en lugar de `await` secuencial innecesario?
- [ ] ¿Toda `Promise` tiene su rechazo manejado, sin "fire-and-forget" accidental?
- [ ] ¿Los errores se traducen a excepciones de dominio donde corresponde, preservando la causa original (`{ cause: error }`)?

Ver también: [`event-loop.md`](./event-loop.md), [`../../anti-patterns/callback-hell.md`](../../anti-patterns/callback-hell.md), [`../../standards/exceptions.md`](../../standards/exceptions.md).

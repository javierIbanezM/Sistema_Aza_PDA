# Knowledge — Node.js

Índice del **runtime Node.js y TypeScript/JavaScript en sí**, independiente de cualquier framework HTTP. Express, NestJS y Fastify tienen cada uno su propia carpeta — no se duplican aquí.

| Documento | Descripción |
|---|---|
| [`event-loop.md`](./event-loop.md) | Modelo de concurrencia, fases del loop, microtasks vs. macrotasks, bloqueo |
| [`async-patterns.md`](./async-patterns.md) | async/await, `Promise.all`/`allSettled`, manejo de errores asíncronos |
| [`streams.md`](./streams.md) | Readable/Writable/Duplex/Transform, `pipeline`, backpressure |
| [`modules.md`](./modules.md) | CommonJS vs. ESM, import dinámico, barrel files, paths absolutos |
| [`typescript.md`](./typescript.md) | `strict`, tipos de utilidad, discriminated unions, branded types |
| [`error-handling.md`](./error-handling.md) | Jerarquía de errores, `Error.cause`, `uncaughtException`/`unhandledRejection` |
| [`performance.md`](./performance.md) | Profiling, Worker Threads, clustering, memory leaks |
| [`debugging.md`](./debugging.md) | Inspector, `AsyncLocalStorage`, depuración de Promises colgadas |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes del runtime/lenguaje |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos de Node.js/TypeScript |
| [`examples.md`](./examples.md) | Casos de uso completos (referencian Express/NestJS/Fastify donde aplica) |

## Dominios relacionados (no duplicados aquí)

| Tecnología | Carpeta |
|---|---|
| Express | [`../express/`](../express/) |
| NestJS | [`../nestjs/`](../nestjs/) |
| Fastify | [`../fastify/`](../fastify/) |

## Cómo usar este índice

- Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí (regla de oro nº2).
- Si el concepto es transversal a cualquier lenguaje (SOLID, DDD, Clean Architecture, testing en general), no lo dupliques aquí — está en [`../architecture/`](../architecture/).

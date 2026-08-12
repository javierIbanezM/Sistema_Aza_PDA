# Best Practices (Node.js — runtime/lenguaje)

> Síntesis de las reglas más importantes del **runtime Node.js y TypeScript puro**. Para Express, NestJS, Fastify, ver el checklist específico de cada uno en su propia carpeta (`../express/`, `../nestjs/`, `../fastify/`).

## Event Loop y asincronía

- Nunca bloquear el Event Loop con cálculo CPU-bound síncrono largo — mover a Worker Threads (ver [`performance.md`](./performance.md), [`event-loop.md`](./event-loop.md)).
- `async/await` como estándar, nunca callback hell (ver [`../../anti-patterns/callback-hell.md`](../../anti-patterns/callback-hell.md)).
- Paralelizar con `Promise.all`/`Promise.allSettled` cuando las operaciones son independientes (ver [`async-patterns.md`](./async-patterns.md)).
- Toda `Promise` con su rechazo manejado — nunca "fire-and-forget" accidental.

## TypeScript

- `strict: true` sin excepciones, `any` prohibido salvo justificación explícita (ver [`typescript.md`](./typescript.md)).
- Discriminated unions con verificación de exhaustividad para conjuntos cerrados de variantes.
- Tipos de utilidad (`Omit`/`Pick`/`Partial`) en lugar de duplicar definiciones.

## Streams

- Procesar archivos/datos grandes con streams, nunca cargando todo en memoria (ver [`streams.md`](./streams.md)).
- `pipeline` (no `.pipe()` manual) para gestión automática de errores y backpressure.

## Errores

- Excepciones de dominio unchecked extendiendo una base común, con `code` explícito (ver [`error-handling.md`](./error-handling.md) y [`../../standards/exceptions.md`](../../standards/exceptions.md)).
- Preservar la causa raíz con `{ cause: error }`.
- `uncaughtException`/`unhandledRejection` terminan el proceso de forma controlada, nunca intentan "continuar".

## Performance

- Medir con el inspector/`perf_hooks` antes de optimizar (ver [`performance.md`](./performance.md)).
- Aprovechar múltiples núcleos con `cluster` o múltiples réplicas orquestadas.

Ver [`pitfalls.md`](./pitfalls.md) para errores frecuentes, y [`examples.md`](./examples.md) para casos de uso completos.

# Examples (Node.js)

Casos de uso completos combinando varios documentos de este ecosistema (Node runtime + Express/NestJS/Fastify).

---

## Ejemplo 1 — Endpoint REST completo con NestJS y Clean Architecture

**Contexto**: exponer `POST /api/v1/pedidos` en NestJS, aplicando Clean Architecture.

**Flujo aplicado**:

1. Dominio (`domain/`): clase `Pedido` con comportamiento propio — ver [`../architecture/ddd.md`](../architecture/ddd.md).
2. Puerto + caso de uso (`application/`): interfaz `PedidoRepository` con token `PEDIDO_REPOSITORY`, `CrearPedidoUseCase` — ver [`../nestjs/architecture.md`](../nestjs/architecture.md).
3. DTO con `class-validator` — ver [`../nestjs/validation.md`](../nestjs/validation.md) y [`../../standards/dto.md`](../../standards/dto.md).
4. Controller delgado, delega en el caso de uso.
5. Exception Filter global mapeando excepciones de dominio a códigos HTTP — ver [`../nestjs/pipes-guards-interceptors.md`](../nestjs/pipes-guards-interceptors.md).
6. Tests: unitarios del caso de uso con Jest (mock/fake del repositorio), sin levantar el contenedor de NestJS.

**Recipe asociada**: [`../../recipes/node-create-rest-endpoint.md`](../../recipes/node-create-rest-endpoint.md).

---

## Ejemplo 2 — Diagnosticar y resolver un Event Loop bloqueado

**Contexto**: un endpoint que genera un reporte CPU-intensivo hace que todas las demás peticiones concurrentes se ralenticen simultáneamente.

**Flujo aplicado**:

1. Se mide la latencia del Event Loop con `monitorEventLoopDelay` — ver [`../node/event-loop.md`](../node/event-loop.md); se confirma un pico coincidente con las peticiones al endpoint de reporte.
2. Se identifica el cálculo síncrono largo dentro del handler — anti-patrón documentado en [`../node/pitfalls.md`](../node/pitfalls.md).
3. Se mueve el cálculo a un Worker Thread — ver [`../node/performance.md`](../node/performance.md).
4. Se vuelve a medir: la latencia del Event Loop para otras peticiones deja de dispararse durante la generación del reporte.

---

## Ejemplo 3 — Migrar de callbacks anidados a async/await

**Contexto**: un módulo legado de procesamiento de pagos usa callbacks anidados de 4 niveles, difícil de mantener y con manejo de errores inconsistente.

**Flujo aplicado** (siguiendo [`../../prompts/refactor.md`](../../prompts/refactor.md)):

1. Se identifica el anti-patrón [Callback Hell](../../anti-patterns/callback-hell.md).
2. Se escriben tests de caracterización sobre el comportamiento actual antes de refactorizar — ver [`../architecture/refactoring.md`](../architecture/refactoring.md).
3. Se envuelven las funciones basadas en callback con `util.promisify` donde la librería no ofrece ya una versión basada en promesas.
4. Se reescribe el flujo con `async/await`, centralizando el manejo de errores en un único `try/catch` — ver [`../node/async-patterns.md`](../node/async-patterns.md).
5. Se identifican operaciones independientes que se ejecutaban secuencialmente sin necesidad, paralelizándolas con `Promise.all`.

---

## Ejemplo 4 — Elegir entre Express, NestJS y Fastify para un servicio nuevo

**Contexto**: nuevo microservicio de notificaciones, equipo pequeño, alto volumen de peticiones esperado.

**Flujo aplicado**:

1. Se descarta Express puro por falta de estructura nativa para un dominio con múltiples casos de uso y necesidad de DI clara — ver [`../nestjs/architecture.md`](../nestjs/architecture.md) frente a organización manual en Express.
2. Se evalúa NestJS (estructura opinionada, DI robusta) vs. Fastify (rendimiento superior, JSON Schema nativo) — ver [`../fastify/fastify.md`](../fastify/fastify.md) sección "Cuándo elegir Fastify".
3. Dado el alto volumen esperado y que el equipo ya modela contratos en JSON Schema para otros servicios, se elige Fastify.
4. Se diseñan los schemas de request/response por endpoint desde el principio, generando después la documentación OpenAPI a partir de ellos — ver [`../architecture/openapi.md`](../architecture/openapi.md).

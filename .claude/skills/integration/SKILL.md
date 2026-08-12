# Skill: Integration

## Rol

Senior Integration Engineer. Guía el diseño e implementación de comunicación entre sistemas: APIs propias consumidas externamente, ERPs, WMS, CRMs, colas de mensajería y servicios de terceros. Transversal a Java, Node y SQL Server.

## Cuándo usar / no usar

**Usar** ante: diseño de un contrato de API consumido por sistemas externos, integración con un ERP/WMS/CRM, diseño de resiliencia (reintentos, Circuit Breaker, idempotencia), Sagas entre múltiples sistemas, sincronización de datos entre sistemas con distinta fuente de verdad, diagnóstico de una integración degradada.

**No usar** ante: comunicación interna dentro de un único servicio/proceso (no cruza un límite de red/sistema real, ver [`../../knowledge/integration/integration-patterns.md`](../../knowledge/integration/integration-patterns.md)), decisiones de arquitectura general no específicas de integración (ir a `skills/architecture/SKILL.md`).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. Consultar `knowledge/integration/README.md` para el índice del dominio.
3. Consultar el documento específico según la tarea (idempotencia, reintentos, Saga, sincronización...).
4. Consultar `patterns/`/`anti-patterns/` (globales) — especialmente `patterns/circuit-breaker.md`, `patterns/adapter.md`.
5. Aplicar `standards/api.md`, `standards/exceptions.md`.
6. Usar la `recipes/integration-*.md` correspondiente si la tarea coincide con una receta ya definida.
7. Implementar con el cambio mínimo necesario.
8. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

[`knowledge/integration/`](../../knowledge/integration/) — índice en [`README.md`](../../knowledge/integration/README.md): `integration-patterns.md`, `api-design.md`, `api-versioning.md`, `idempotency.md`, `retries-and-backoff.md`, `circuit-breaker.md`, `saga.md`, `event-driven.md`, `message-queues.md`, `schema-mapping.md`, `data-synchronization.md`, `observability.md`, `distributed-tracing.md`, `error-handling.md`, `best-practices.md`, `pitfalls.md`, `examples.md`.

Para principios transversales: [`knowledge/architecture/`](../../knowledge/architecture/) — no duplicado aquí.

## Patrones a consultar

[`patterns/`](../../patterns/) (global): `circuit-breaker.md`, `adapter.md`, `observer.md`.

## Anti-patrones a consultar

[`anti-patterns/`](../../anti-patterns/) (global): `sql-injection.md` (si la integración implica SQL dinámico), `god-object.md` (orquestadores de Saga que acumulan lógica no relacionada).

## Standards a aplicar

[`standards/`](../../standards/) (global): `api.md`, `dto.md`, `exceptions.md`, `logging-standard.md`, `validation.md`.

## Checklists a ejecutar

[`checklists/architecture.md`](../../checklists/architecture.md).

## Prompts a utilizar

[`prompts/`](../../prompts/) (global): `review.md`, `debug.md`, `performance.md`.

## Recipes a utilizar

[`recipes/`](../../recipes/) (plano, prefijo `integration-`): `integration-add-idempotency.md`, `integration-create-resilient-client.md`, `integration-create-saga.md`.

## Snippets a reutilizar

[`snippets/integration/`](../../snippets/integration/).

## Decisions relevantes

[`decisions/`](../../decisions/): `rest-vs-graphql.md`.

## Ejemplos

[`knowledge/integration/examples.md`](../../knowledge/integration/examples.md).

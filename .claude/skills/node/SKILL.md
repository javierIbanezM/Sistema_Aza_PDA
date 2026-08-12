# Skill: Node

## Rol

Senior Node.js/TypeScript Developer. Guía la implementación de servicios backend en Node.js, aplicando siempre primero las reglas transversales de la Skill `architecture` (SOLID, DDD, Clean Architecture) y, sobre ellas, las especificidades del runtime Node.js y el framework HTTP elegido (Express, NestJS, Fastify), cada uno en su propia carpeta de `knowledge/`.

## Cuándo usar / no usar

**Usar** ante: cualquier tarea de implementación, revisión o diagnóstico sobre código Node.js/TypeScript (endpoints, casos de uso, middlewares/guards/plugins, tests, configuración de build).

**No usar** ante: decisiones de arquitectura general no específicas de Node (ir primero a `skills/architecture/SKILL.md`), persistencia específica de SQL Server más allá del ORM elegido (ir a `skills/sqlserver/SKILL.md`), infraestructura de despliegue (ir a la Skill de DevOps/Cloud correspondiente).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. Identificar si la tarea es del runtime Node/TypeScript en sí, o de un framework HTTP concreto, y cargar el `README.md` de `knowledge/<tecnología>/` correspondiente.
3. Consultar el documento específico dentro de esa carpeta.
4. Consultar `patterns/` y `anti-patterns/` (globales) — especialmente `callback-hell.md`, `god-object.md`, `anemic-domain-model.md`.
5. Aplicar `standards/` (globales) — naming, DTOs, API, excepciones, logging, validación.
6. Usar la `recipes/node-*.md` correspondiente si la tarea coincide con una receta ya definida.
7. Implementar con el cambio mínimo necesario.
8. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

| Tecnología | Carpeta | Contenido |
|---|---|---|
| Node.js (runtime/lenguaje) | [`knowledge/node/`](../../knowledge/node/) | `event-loop.md`, `async-patterns.md`, `streams.md`, `modules.md`, `typescript.md`, `error-handling.md`, `performance.md`, `debugging.md`, `best-practices.md`, `pitfalls.md`, `examples.md` |
| Express | [`knowledge/express/`](../../knowledge/express/) | `routing.md`, `middleware.md` |
| NestJS | [`knowledge/nestjs/`](../../knowledge/nestjs/) | `architecture.md`, `pipes-guards-interceptors.md`, `validation.md` |
| Fastify | [`knowledge/fastify/`](../../knowledge/fastify/) | `fastify.md` |

Para principios transversales (SOLID, DDD, Clean Architecture, testing, refactoring, debugging general, logging): [`knowledge/architecture/`](../../knowledge/architecture/) — no duplicado aquí.

## Patrones a consultar

[`patterns/`](../../patterns/) (global): especialmente `repository.md`, `observer.md` (eventos de dominio), `adapter.md` (integración con SDKs externos), `decorator.md` (middlewares/interceptors).

## Anti-patrones a consultar

[`anti-patterns/`](../../anti-patterns/) (global): especialmente `callback-hell.md`, `memory-leak.md` (closures en listeners de larga duración), `anemic-domain-model.md`, `god-object.md`.

## Standards a aplicar

[`standards/`](../../standards/) (global): `naming.md`, `git.md`, `commits.md`, `branching.md`, `logging-standard.md`, `exceptions.md`, `validation.md`, `dto.md`, `api.md`.

## Checklists a ejecutar

[`checklists/architecture.md`](../../checklists/architecture.md).

## Prompts a utilizar

[`prompts/`](../../prompts/) (global): `review.md`, `refactor.md`, `generate-tests.md`, `debug.md`, `performance.md`.

## Recipes a utilizar

[`recipes/`](../../recipes/) (plano, prefijo `node-`): `node-create-rest-endpoint.md`, `node-create-service.md`, `node-create-middleware.md`, `node-create-websocket.md`.

## Snippets a reutilizar

[`snippets/node/`](../../snippets/node/) — ver [`snippets/README.md`](../../snippets/README.md).

## Decisions relevantes

[`decisions/`](../../decisions/) (global): `rest-vs-graphql.md`, `jwt-vs-session.md`.

## Ejemplos

[`knowledge/node/examples.md`](../../knowledge/node/examples.md).

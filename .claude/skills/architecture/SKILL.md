# Skill: Architecture

## Rol

Arquitecto de software senior. Guía las decisiones de diseño de sistema y de código que son transversales a cualquier stack: principios SOLID/DRY/KISS/YAGNI, modelado de dominio (DDD), organización en capas (Clean Architecture), estrategia de testing, refactoring, debugging, logging/observabilidad y documentación técnica.

## Cuándo usar / no usar

**Usar** ante: diseño de un módulo/servicio nuevo, decisiones de capas y carpetas, modelado de dominio, evaluación de un patrón de diseño, síntomas de mala arquitectura, planificación de un refactor de envergadura, definición de estrategia de testing, redacción de un ADR/RFC.

**No usar** ante: sintaxis específica de un lenguaje/framework (ir a la Skill de esa tecnología), configuración de infraestructura (ir a la Skill de DevOps/Cloud), o cuando ya existe un ADR aceptado para la decisión y la tarea es solo implementarlo.

## Flujo

1. Leer `CLAUDE.md` y los ADRs existentes del proyecto.
2. Consultar `knowledge/architecture/` según el tipo de decisión.
3. Consultar `patterns/` y `anti-patterns/` antes de implementar.
4. Si la decisión es relevante, generar un ADR (`templates/ADR.md`).
5. Implementar con el cambio mínimo necesario.
6. Pasar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

[`knowledge/architecture/`](../../knowledge/architecture/) — índice en [`README.md`](../../knowledge/architecture/README.md): `solid.md`, `dry-kiss-yagni.md`, `ddd.md`, `clean-architecture.md`, `refactoring.md`, `testing.md`, `debugging.md`, `logging.md`, `technical-documentation.md`, `openapi.md`.

## Patrones a consultar

[`patterns/`](../../patterns/) (global): `repository.md`, `factory.md`, `builder.md`, `strategy.md`, `observer.md`, `cqrs.md`, `circuit-breaker.md`.

## Anti-patrones a consultar

[`anti-patterns/`](../../anti-patterns/) (global): `god-object.md`, `spaghetti.md`, `anemic-domain-model.md`, `callback-hell.md`, `n-plus-one.md`, `sql-injection.md`, `memory-leak.md`.

## Standards a aplicar

[`standards/`](../../standards/) (global): `naming.md`, `git.md`, `commits.md`, `branching.md`, `logging-standard.md`, `exceptions.md`, `validation.md`, `dto.md`, `api.md`.

## Checklists a ejecutar

[`checklists/architecture.md`](../../checklists/architecture.md).

## Prompts a utilizar

[`prompts/`](../../prompts/) (global): `review.md`, `refactor.md`, `generate-tests.md`, `debug.md`, `performance.md`.

## Recipes a utilizar

Ninguna específica de este dominio — las recipes por tecnología viven en `recipes/<dominio>/` (se irán añadiendo a medida que se construyan los dominios java/node/android/sqlserver).

## Playbooks a utilizar

[`playbooks/`](../../playbooks/) (se construyen en un milestone posterior): cualquier playbook que implique diseño de un módulo/servicio nuevo consulta esta Skill como primer paso.

## Ejemplos

[`knowledge/architecture/examples.md`](../../knowledge/architecture/examples.md).

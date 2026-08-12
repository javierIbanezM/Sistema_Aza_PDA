# Documentación — Arquitectura de Software

Índice de toda la documentación profunda de este dominio. Cada documento trata **un único tema** en profundidad. No hay contenido duplicado entre ellos: si dos documentos necesitan referirse al mismo concepto, uno enlaza al otro en lugar de repetirlo.

| Documento | Descripción |
|---|---|
| [`solid.md`](./solid.md) | Los cinco principios SOLID explicados con código antes/después, señales de violación y cómo corregirlas. |
| [`dry-kiss-yagni.md`](./dry-kiss-yagni.md) | DRY, KISS y YAGNI: definición precisa, límites de aplicación y el error común de sobre-aplicarlos. |
| [`ddd.md`](./ddd.md) | Domain-Driven Design estratégico y táctico: bounded contexts, lenguaje ubicuo, entidades, value objects, agregados, repositorios, domain services, domain events, anti-corruption layer. |
| [`clean-architecture.md`](./clean-architecture.md) | Arquitectura limpia y hexagonal: capas, regla de dependencia, puertos/adaptadores, mapeo entre capas, organización de carpetas real. |
| [`refactoring.md`](./refactoring.md) | Metodología de refactoring seguro: tests de caracterización, refactors pequeños, catálogo de refactors comunes. |
| [`testing.md`](./testing.md) | Pirámide de testing, tipos de test (unitario, integración, contrato, E2E), nomenclatura, mocks vs stubs vs fakes. |
| [`debugging.md`](./debugging.md) | Metodología sistemática para depurar un fallo: reproducción, aislamiento, hipótesis, bisección. |
| [`logging.md`](./logging.md) | Logging estructurado, niveles de log, correlación de peticiones, métricas, trazas distribuidas, SLIs/SLOs. |
| [`documentacion-tecnica.md`](./technical-documentation.md) | Cómo escribir ADRs, RFCs, READMEs y documentación que se mantiene viva junto al código. |
| [`openapi.md`](./openapi.md) | Documentar APIs REST con OpenAPI 3.x / Swagger: estructura del contrato, ejemplos, versionado. |
| [`adr/`](./adr/README.md) | Architecture Decision Records del propio framework (decisiones ya tomadas y su justificación) |

## Cómo usar este índice

- Si necesitas profundizar en un tema, ve directo al documento correspondiente — no busques la explicación en `SKILL.md`, ahí solo hay enlaces.
- Si vas a crear un documento nuevo sobre un tema no cubierto aquí, añádelo a esta tabla y enlázalo también desde `SKILL.md` si es un punto de entrada relevante.
- Antes de escribir una explicación nueva en cualquier otra Skill del framework (Java, Node, Android...), comprueba primero si el concepto ya está documentado aquí. Si lo está, **enlaza, no copies**.

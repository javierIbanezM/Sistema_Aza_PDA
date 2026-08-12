# ADR — Índice

Architecture Decision Records del propio framework `claude-code-premium` — decisiones estructurales que otros documentos (standards, patterns, recipes) asumen como ya tomadas. Usan la plantilla [`../../../templates/ADR.md`](../../../templates/ADR.md).

| ADR | Decisión |
|---|---|
| [`ADR-0001-clean-architecture.md`](./ADR-0001-clean-architecture.md) | Clean Architecture como organización en capas por defecto |
| [`ADR-0002-repository-pattern.md`](./ADR-0002-repository-pattern.md) | Repository como patrón de persistencia por defecto |
| [`ADR-0003-rest-over-graphql.md`](./ADR-0003-rest-over-graphql.md) | REST como estilo de API por defecto |

Estos ADRs son inmutables una vez aceptados (ver [`../technical-documentation.md`](../technical-documentation.md)) — si una decisión cambia, se documenta un ADR nuevo que referencia y supersede al anterior.

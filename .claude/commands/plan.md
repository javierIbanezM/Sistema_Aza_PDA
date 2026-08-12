# /plan

## Qué hace

Convierte un diseño ya acordado en un plan de implementación por pasos verificables, con riesgos identificados antes de empezar.

## Invoca

- Agente: [`../agents/tech-lead.md`](../agents/tech-lead.md).
- Requiere un diseño previo — si no existe, primero `/architecture` (o directamente [`../agents/software-architect.md`](../agents/software-architect.md) para el caso concreto).
- Produce: pasos con criterio de "hecho" individual, dependencias entre pasos, riesgos técnicos con mitigación, y qué agente técnico ejecuta cada paso (según `docs/orchestrator.md` Paso 2b).

## Cuándo usarlo

Tras cerrar el diseño de una funcionalidad o cambio no trivial, antes de empezar a implementar — nunca se planifica sobre un diseño ambiguo (el agente lo rechaza explícitamente si detecta esa ambigüedad).

## Equivalente manual

Consultar [`../agents/tech-lead.md`](../agents/tech-lead.md) directamente.

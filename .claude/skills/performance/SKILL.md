# Skill: Performance

## Rol

Senior Performance Engineer. Guía metodología de rendimiento transversal — nunca empieza por herramientas, siempre por: Medir → Localizar cuello de botella → Datos → Hipótesis → Experimento → Medición → Optimización → Validación.

## Cuándo usar / no usar

**Usar** ante: diagnóstico de un problema de rendimiento, planificación de capacidad, diseño de estrategia de caché, pruebas de carga, detección de regresión de rendimiento, decisión de escalado horizontal vs. vertical.

**No usar** ante: diagnóstico específico de un stack ya cubierto (ir a `knowledge/java/performance.md`, `knowledge/node/performance.md`, `knowledge/sqlserver/performance.md`, `knowledge/android/performance.md`), observabilidad de plataforma general (ir a `skills/devops/`).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. **Nunca empezar por herramientas** — cuantificar el problema con `knowledge/performance/performance-budget.md` antes de proponer cualquier optimización.
3. Localizar el cuello de botella con `knowledge/performance/bottleneck-analysis.md` (método USE), después profundizar con el documento específico del stack afectado.
4. Formular hipótesis falsable con `knowledge/performance/performance-engineering.md` antes de experimentar.
5. Usar `recipes/performance-run-load-test.md` si la tarea requiere pruebas de carga.
6. Validar la mejora con la misma metodología de medición que la línea base.
7. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

[`knowledge/performance/`](../../knowledge/performance/) — 11 documentos transversales (ver README). Para diagnóstico específico: `knowledge/java/performance.md`, `knowledge/node/performance.md`, `knowledge/sqlserver/performance.md`, `knowledge/android/performance.md`, `knowledge/compose/performance.md`.

## Decisions relevantes

[`decisions/horizontal-vs-vertical-scaling.md`](../../decisions/horizontal-vs-vertical-scaling.md), [`decisions/rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md), [`decisions/ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md).

## Recipes a utilizar

[`recipes/performance-run-load-test.md`](../../recipes/performance-run-load-test.md).

## Snippets a reutilizar

[`snippets/performance/`](../../snippets/performance/).

## Playbooks a ejecutar

[`playbooks/performance.md`](../../playbooks/performance.md).

## Prompts a utilizar

[`prompts/performance.md`](../../prompts/performance.md).

## Ejemplos

[`knowledge/performance/examples.md`](../../knowledge/performance/examples.md).

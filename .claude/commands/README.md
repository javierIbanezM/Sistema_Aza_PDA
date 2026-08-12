# Commands — Índice

Comandos slash del framework. Cada uno es **únicamente un orquestador**: decide qué playbook y qué agente(s) invocar, nunca contiene conocimiento técnico propio — ese conocimiento vive en `knowledge/`, `agents/` y `playbooks/`, y los comandos solo enlazan a él.

| Comando | Invoca | Cuándo |
|---|---|---|
| [`/analyze`](./analyze.md) | Project Analyzer Nivel 1 (`docs/orchestrator.md` Paso 2) | Confirmar qué agentes recomienda el framework para el proyecto actual |
| [`/review`](./review.md) | Code Reviewer → Security → Performance → Checklist | Antes de dar un cambio no trivial por terminado |
| [`/debug`](./debug.md) | Senior Debugger, proceso completo con test de regresión | Ante cualquier fallo de causa no evidente |
| [`/feature`](./feature.md) | `playbooks/new-feature.md` | Funcionalidad nueva con ambigüedad real |
| [`/refactor`](./refactor.md) | `playbooks/refactor.md` | Cambio estructural sin alterar comportamiento |
| [`/security`](./security.md) | Threat model (diseño) o `playbooks/security-issue.md` (incidente) | Diseño con datos sensibles, o vulnerabilidad detectada |
| [`/performance`](./performance.md) | `playbooks/performance.md` | Lentitud reportada o capacity planning |
| [`/architecture`](./architecture.md) | `agents/software-architect.md` | Decisión de diseño difícil de revertir |
| [`/plan`](./plan.md) | `agents/tech-lead.md` | Convertir un diseño ya acordado en plan de implementación |
| [`/implement`](./implement.md) | Agente técnico según tecnología (`docs/orchestrator.md` Paso 2b) | Ejecutar un paso de implementación concreto |
| [`/document`](./document.md) | ADR/RFC/OpenAPI según el tipo de cambio | Al cerrar cualquier tarea con documentación asociada |
| [`/test`](./test.md) | `prompts/generate-tests.md` | Al completar cualquier capa de una implementación |
| [`/release`](./release.md) | `playbooks/release-management.md` | Cerrar un ciclo que agrupa varios cambios |

## Principio de diseño

Ningún comando duplica lo que ya existe en `playbooks/`, `agents/` o `knowledge/` — cada uno es deliberadamente corto (30-50 líneas), y su única función es **decidir y enlazar**, nunca explicar. Si un comando empieza a acumular contenido técnico propio, esa es la señal de que ese contenido pertenece a `knowledge/` y el comando debería enlazarlo en su lugar.

## Relación con `docs/orchestrator.md`

Los comandos son el atajo directo a decisiones que el orquestador ya sabe tomar — `/feature` no reemplaza al orquestador, es la forma más rápida de invocar exactamente lo que el orquestador seleccionaría de todas formas al detectar una tarea de tipo "funcionalidad nueva". Ver [`../docs/orchestrator.md`](../docs/orchestrator.md).

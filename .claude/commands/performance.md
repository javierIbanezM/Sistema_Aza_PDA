# /performance

## Qué hace

Ejecuta el proceso de diagnóstico y optimización de rendimiento — nunca optimiza por intuición.

## Invoca

- Playbook: [`../playbooks/performance.md`](../playbooks/performance.md).
- Cadena de agentes: Senior Business Analyst (cuantifica el objetivo en percentiles) → Senior Debugger + Senior Performance Engineer (localizan el cuello de botella con datos) → Software Architect (solo si la causa es estructural) → Tech Lead → Agente técnico según tecnología detectada → Senior Code Reviewer.

Metodología innegociable: **Medir → Localizar cuello de botella → Datos → Hipótesis → Experimento → Medición → Optimización → Validación** (ver [`../agents/senior-performance-engineer.md`](../agents/senior-performance-engineer.md)).

Cierre con [`../checklists/performance.md`](../checklists/performance.md).

## Cuándo usarlo

Ante cualquier reporte de lentitud, timeout, o antes de un evento de tráfico conocido (capacity planning).

## Equivalente manual

Seguir [`../playbooks/performance.md`](../playbooks/performance.md) fase a fase.

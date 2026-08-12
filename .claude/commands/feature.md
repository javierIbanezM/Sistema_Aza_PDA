# /feature

## Qué hace

Ejecuta el proceso completo de una funcionalidad nueva no trivial, de principio a fin.

## Invoca

- Playbook: [`../playbooks/new-feature.md`](../playbooks/new-feature.md).
- Cadena de agentes (fija, el playbook la detalla fase a fase): Senior Business Analyst → Software Architect → Tech Lead → Agente técnico según tecnología detectada (ver `docs/orchestrator.md` Paso 2b) → Senior Code Reviewer → Senior Debugger (solo si hay incidencias).

## Cuándo usarlo

Cualquier funcionalidad nueva con ambigüedad real de requisitos o diseño — para una tarea pequeña y sin ambigüedad, usar directamente el agente técnico correspondiente sin forzar el proceso completo (ver `docs/orchestrator.md`, principio de proporcionalidad).

## Equivalente manual

Seguir [`../playbooks/new-feature.md`](../playbooks/new-feature.md) fase a fase.

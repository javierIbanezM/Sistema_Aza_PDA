# /refactor

## Qué hace

Ejecuta un cambio estructural sin alterar comportamiento observable, con la red de seguridad de tests como condición previa obligatoria.

## Invoca

- Playbook: [`../playbooks/refactor.md`](../playbooks/refactor.md).
- Cadena de agentes: Software Architect (justifica el refactor contra un anti-patrón real) → Tech Lead (verifica tests existentes, divide en pasos) → Agente técnico según tecnología detectada → Senior Code Reviewer.

## Cuándo usarlo

Ante un anti-patrón identificado ([`../anti-patterns/`](../anti-patterns/)) que justifica el cambio — nunca "porque sí" o por preferencia de estilo sin problema real de mantenibilidad.

## Regla de oro heredada del playbook

Sin tests previos suficientes, escribirlos es un paso obligatorio antes de tocar el código, no opcional.

## Equivalente manual

Seguir [`../playbooks/refactor.md`](../playbooks/refactor.md) fase a fase.

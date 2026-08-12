# /review

## Qué hace

Ejecuta una revisión completa del cambio actual, combinando calidad de código, seguridad y rendimiento en una única pasada.

## Invoca

```
Senior Code Reviewer
        ↓
Senior Security Engineer   (checklist de seguridad si el cambio lo justifica)
        ↓
Senior Performance Engineer (checklist de rendimiento si el cambio lo justifica)
        ↓
Checklist de cierre
        ↓
Informe consolidado
```

- Agente principal: [`../agents/senior-code-reviewer.md`](../agents/senior-code-reviewer.md), siguiendo [`../prompts/review.md`](../prompts/review.md).
- Si el cambio toca autenticación/autorización/datos sensibles: añade [`../agents/senior-security-engineer.md`](../agents/senior-security-engineer.md) con [`../checklists/security.md`](../checklists/security.md).
- Si el cambio toca una ruta de alta frecuencia o una query/algoritmo: añade [`../agents/senior-performance-engineer.md`](../agents/senior-performance-engineer.md) con [`../checklists/performance.md`](../checklists/performance.md).
- Cierre siempre con [`../checklists/architecture.md`](../checklists/architecture.md).

## Cuándo usarlo

Antes de dar cualquier cambio no trivial por terminado — el atajo directo al flujo que, de otro modo, requeriría invocar cada agente por separado.

## Equivalente manual

Ejecutar [`../prompts/review.md`](../prompts/review.md) y, según el alcance del cambio, los checklists de [`../checklists/`](../checklists/) relevantes uno a uno.

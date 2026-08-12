# Playbook: New Feature

## Cuándo usar este playbook

Cualquier funcionalidad nueva no trivial — desde una petición de negocio hasta el código en producción. Para cambios muy pequeños y bien acotados sin ambigüedad de diseño, una `recipe/` directa puede ser suficiente sin pasar por el flujo completo (ver criterio de proporcionalidad en [`../agents/tech-lead.md`](../agents/tech-lead.md), punto 2).

## Fases

### 1. Senior Business Analyst

- **Hace**: define el problema real (no la solución propuesta), identifica requisitos, detecta casos límite y de error, cuantifica lo ambiguo.
- **Produce**: documento de análisis con problema de negocio, criterios de aceptación verificables, casos límite identificados.
- **Entrega a**: Software Architect.
- **Terminada cuando**: cada criterio de aceptación es verificable sin interpretación adicional (ver formato de salida en [`../agents/senior-business-analyst.md`](../agents/senior-business-analyst.md)).

### 2. Software Architect

- **Hace**: diseña la solución, elige patrones de [`../patterns/`](../patterns/), evalúa impacto y trade-offs, clasifica si la decisión requiere ADR.
- **Produce**: diseño de alto nivel (capas, contextos, patrones elegidos) + ADR si la decisión es de alto impacto (ver [`../templates/ADR.md`](../templates/ADR.md)).
- **Entrega a**: Tech Lead.
- **Terminada cuando**: cada patrón/decisión tiene su trade-off explícito documentado (ver formato de salida en [`../agents/software-architect.md`](../agents/software-architect.md)).

### 3. Tech Lead

- **Hace**: divide el trabajo en pasos verificables de forma independiente, identifica dependencias y riesgos técnicos, asigna cada paso a la Skill/dominio correspondiente.
- **Produce**: plan de implementación por pasos, con criterio de "hecho" de cada uno.
- **Entrega a**: la(s) Skill(s) técnica(s) correspondiente(s) (`skills/java/`, `skills/node/`, `skills/sqlserver/`...).
- **Terminada cuando**: cada paso del plan es verificable de forma aislada y tiene sus riesgos identificados (ver formato de salida en [`../agents/tech-lead.md`](../agents/tech-lead.md)).

### 4. Agente técnico (según tecnología detectada)

- **Hace**: implementa cada paso del plan siguiendo su `SKILL.md`, las `recipes/` aplicables, `standards/` y `patterns/`/`anti-patterns/` globales.
- **Produce**: código implementado + tests (ver [`../prompts/generate-tests.md`](../prompts/generate-tests.md)) para cada paso.
- **Entrega a**: Senior Code Reviewer.
- **Terminada cuando**: pasa el checklist [`../checklists/architecture.md`](../checklists/architecture.md) y el criterio de "hecho" de cada paso del Tech Lead.

El agente concreto lo determina `docs/orchestrator.md` Paso 2b según la tecnología detectada — por ejemplo, [`../agents/senior-java-engineer.md`](../agents/senior-java-engineer.md) para Java/Spring, [`../agents/senior-nodejs-engineer.md`](../agents/senior-nodejs-engineer.md) para Node.js, [`../agents/senior-android-engineer.md`](../agents/senior-android-engineer.md) para Android. Si el cambio toca más de una capa (aplicación + base de datos, por ejemplo), participan varios agentes técnicos en la secuencia que `docs/orchestrator.md` especifica (aplicación → persistencia → infraestructura).

### 5. Senior Code Reviewer

- **Hace**: revisa en capas (diseño → anti-patrones → seguridad → tests → estilo, ver [`../agents/senior-code-reviewer.md`](../agents/senior-code-reviewer.md)), con severidad explícita por hallazgo.
- **Produce**: revisión con hallazgos bloqueantes/importantes/sugerencias, o aprobación.
- **Entrega a**: de vuelta al agente técnico si hay hallazgos bloqueantes; si no, el trabajo queda listo para desplegar.
- **Terminada cuando**: no quedan hallazgos `[BLOQUEANTE]` sin resolver.

### 6. Senior Debugger (solo si hay incidencias)

- **Hace**: si algo falla durante la implementación o tras el despliegue, aplica depuración sistemática (ver [`../agents/senior-debugger.md`](../agents/senior-debugger.md)) — reproducción confirmada, hipótesis falsable, causa raíz, test de regresión.
- **Produce**: fix con test de regresión.
- **Entrega a**: Senior Code Reviewer (el fix también se revisa).
- **Terminada cuando**: el test de regresión falla sin el fix y pasa con él.

## Diagrama de flujo

```
Senior Business Analyst
        ↓ (análisis + criterios de aceptación)
Software Architect
        ↓ (diseño + ADR si aplica)
Tech Lead
        ↓ (plan por pasos)
Agente técnico ←──────────────┐
        ↓ (código + tests)   │ (si hay hallazgos bloqueantes)
Senior Code Reviewer ─────────┘
        ↓ (aprobado)
   [Producción]
        ↓ (si algo falla)
Senior Debugger → Senior Code Reviewer (revisa el fix)
```

## Documentos que nunca se duplican en este playbook

Este playbook no repite ninguna regla de `standards/`, ningún patrón de `patterns/`, ninguna metodología de los agentes — solo secuencia quién hace qué y qué se entrega entre fases.

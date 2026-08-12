# Playbook: Bug Fix

## Cuándo usar este playbook

Un defecto reportado en funcionalidad existente, **no urgente** (para incidentes en producción que requieren corrección inmediata, ver [`hotfix.md`](./hotfix.md)).

## Fases

### 1. Senior Business Analyst

- **Hace**: confirma el comportamiento esperado real (¿es efectivamente un bug, o una expectativa no documentada?), identifica el impacto de negocio y la severidad.
- **Produce**: descripción del comportamiento esperado vs. observado, severidad, usuarios/casos afectados.
- **Entrega a**: Senior Debugger.
- **Terminada cuando**: está claro y verificable qué comportamiento se considera correcto.

### 2. Senior Debugger

- **Hace**: aplica depuración sistemática completa (ver [`../agents/senior-debugger.md`](../agents/senior-debugger.md)) — reproducción confirmada, hipótesis, causa raíz, corrección con test de regresión.
- **Produce**: fix + test que falla sin el fix y pasa con él.
- **Entrega a**: Senior Code Reviewer.
- **Terminada cuando**: la causa raíz está identificada (no solo el síntoma) y el test de regresión lo demuestra.

### 3. Senior Code Reviewer

- **Hace**: revisa el fix con el mismo rigor que cualquier cambio de producción — especialmente si la corrección realmente ataca la causa raíz, no un parche superficial.
- **Produce**: aprobación o hallazgos.
- **Entrega a**: despliegue si aprobado; de vuelta al Debugger si el fix es superficial.
- **Terminada cuando**: no quedan hallazgos bloqueantes.

### 4. Software Architect (solo si la causa raíz es de diseño)

- **Hace**: si el Debugger determina que la causa raíz es una decisión arquitectónica (no un error de implementación puntual), este agente evalúa si requiere un cambio más amplio que el propio fix.
- **Produce**: recomendación de alcance (¿el fix puntual es suficiente, o hace falta abrir un [`refactor.md`](./refactor.md) separado?).
- **Entrega a**: Tech Lead si se requiere trabajo adicional planificado.

## Diagrama de flujo

```
Senior Business Analyst
        ↓ (comportamiento esperado confirmado)
Senior Debugger
        ↓ (causa raíz + fix + test de regresión)
   ¿causa raíz es de diseño?
        │
   No ──┴── Sí → Software Architect → (si aplica) Tech Lead → new-feature.md / refactor.md
        ↓
Senior Code Reviewer
        ↓ (aprobado)
   [Producción]
```

Ver también: [`hotfix.md`](./hotfix.md) para la variante urgente, [`refactor.md`](./refactor.md) si la causa raíz requiere trabajo estructural más amplio.

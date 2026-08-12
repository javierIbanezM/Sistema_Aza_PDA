# /debug

## Qué hace

Aplica el proceso de depuración sistemática del framework ante un fallo — nunca "prueba a ver si esto lo arregla".

## Invoca

```
Senior Debugger
        ↓
Logs / evidencia (reproducción confirmada)
        ↓
Hipótesis (falsable, un experimento concreto)
        ↓
Prueba (cambia una única variable)
        ↓
Fix
        ↓
Test de regresión (falla sin el fix, pasa con él)
```

- Agente: [`../agents/senior-debugger.md`](../agents/senior-debugger.md), siguiendo [`../prompts/debug.md`](../prompts/debug.md) y la metodología de [`../knowledge/architecture/debugging.md`](../knowledge/architecture/debugging.md).
- Herramientas específicas según el stack detectado (ver `docs/orchestrator.md` Paso 2b): `knowledge/java/debugging.md`, `knowledge/node/debugging.md`, `knowledge/sqlserver/blocking.md`/`deadlocks.md`, `knowledge/android/debugging.md`, `knowledge/javascript/debugging.md`.
- Si la causa raíz resulta ser una decisión de diseño, no un bug de implementación: escala a [`../agents/software-architect.md`](../agents/software-architect.md).

## Cuándo usarlo

Ante cualquier fallo cuya causa no es evidente de inmediato — el comando fuerza el proceso completo (reproducción → hipótesis → experimento → test de regresión) en lugar de saltar directamente a "probar una corrección".

## Equivalente manual

Ejecutar [`../prompts/debug.md`](../prompts/debug.md) directamente.

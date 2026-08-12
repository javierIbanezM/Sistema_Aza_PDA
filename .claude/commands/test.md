# /test

## Qué hace

Genera la suite de tests correspondiente a un cambio, siguiendo la pirámide de testing del framework.

## Invoca

- Prompt: [`../prompts/generate-tests.md`](../prompts/generate-tests.md).
- Metodología: [`../knowledge/architecture/testing.md`](../knowledge/architecture/testing.md) — camino feliz + validación fallida + caso límite como cobertura mínima, nunca un porcentaje arbitrario.
- Herramientas específicas del stack detectado (`docs/orchestrator.md` Paso 2b): [`../knowledge/junit/junit.md`](../knowledge/junit/junit.md)/[`../knowledge/mockito/mockito.md`](../knowledge/mockito/mockito.md) para Java, Jest para Node/JavaScript, Compose Testing para Android.

## Cuándo usarlo

Al completar cualquier capa de una implementación (dominio, caso de uso, adaptador) — ver los agentes técnicos individuales, que ya integran este paso en su propia metodología en lugar de dejarlo para el final.

## Equivalente manual

Ejecutar [`../prompts/generate-tests.md`](../prompts/generate-tests.md) directamente.

# /implement

## Qué hace

Ejecuta un paso de implementación concreto ya planificado, delegando en el agente técnico correcto según la tecnología.

## Invoca

- Selección de agente: `docs/orchestrator.md` Paso 2b (tabla Dominio→Agente).
- Si existe una recipe para la tarea concreta (crear un endpoint, crear un índice, desplegar un servicio...), el agente la usa como base — ver [`../recipes/README.md`](../recipes/README.md) para el catálogo completo por dominio.
- Snippets de [`../snippets/`](../snippets/) como código de partida, nunca copiados sin adaptar al contexto real.

## Cuándo usarlo

Cuando ya existe un plan (propio o del Tech Lead vía `/plan`) y toca ejecutar un paso concreto — para tareas pequeñas sin plan previo necesario, se usa igualmente para ir directo al agente técnico correspondiente.

## Equivalente manual

Identificar la tecnología del cambio y consultar el `SKILL.md` correspondiente en [`../skills/`](../skills/).

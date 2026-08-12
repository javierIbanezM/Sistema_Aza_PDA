# Playbook: Performance Issue

## Cuándo usar este playbook

Un problema de rendimiento reportado (latencia alta, timeout, consumo de recursos elevado) — desde el diagnóstico inicial hasta la verificación de la mejora.

## Fases

### 1. Senior Business Analyst

- **Hace**: cuantifica el problema en términos verificables — ¿"lento" significa qué percentil, con qué volumen de datos, para qué usuarios? (ver [`../agents/senior-business-analyst.md`](../agents/senior-business-analyst.md), punto 3).
- **Produce**: síntoma cuantificado y objetivo de mejora explícito (ej. "p95 de 3s a p95 de 300ms").
- **Entrega a**: Senior Debugger.
- **Terminada cuando**: existe un número objetivo verificable, no solo "que vaya más rápido".

### 2. Senior Debugger + Senior Performance Engineer

- **Hace**: aplica el proceso de diagnóstico de rendimiento (ver [`../prompts/performance.md`](../prompts/performance.md) y [`../agents/senior-performance-engineer.md`](../agents/senior-performance-engineer.md), metodología Medir → Localizar cuello de botella → Datos → Hipótesis → Experimento) — mide antes de optimizar, identifica el cuello de botella real (CPU/I/O/contención/GC) con herramientas del stack correspondiente (`knowledge/java/performance.md`, `knowledge/node/performance.md`, `knowledge/sqlserver/performance.md`).
- **Produce**: cuello de botella identificado con evidencia medida (profiling, plan de ejecución, wait stats según el stack), no supuesto.
- **Entrega a**: Software Architect si la causa es estructural; el agente técnico correspondiente directamente si es una optimización acotada (ej. un índice faltante, una query non-SARGable).
- **Terminada cuando**: la causa está confirmada con medición real, no solo hipótesis.

### 3. Software Architect (solo si la causa es estructural)

- **Hace**: si el cuello de botella requiere un cambio de patrón (introducir caché, CQRS, particionamiento — ver [`../patterns/cqrs.md`](../patterns/cqrs.md), [`../knowledge/sqlserver/partitioning.md`](../knowledge/sqlserver/partitioning.md)), evalúa el trade-off frente a la complejidad que introduce.
- **Produce**: decisión de diseño para la optimización, con ADR si el impacto es alto.
- **Entrega a**: Tech Lead.

### 4. Tech Lead (solo si el cambio requiere planificación)

- **Hace**: si la optimización es más que un cambio puntual, la divide en pasos verificables.
- **Produce**: plan de implementación.
- **Entrega a**: el agente técnico correspondiente a la tecnología del componente optimizado.

### 5. Agente técnico (según tecnología detectada)

- **Hace**: implementa la optimización (índice → [`../agents/senior-sqlserver-dba.md`](../agents/senior-sqlserver-dba.md), caché/Worker Thread → [`../agents/senior-nodejs-engineer.md`](../agents/senior-nodejs-engineer.md)/[`../agents/senior-java-engineer.md`](../agents/senior-java-engineer.md), autoescalado/capacidad → [`../agents/senior-aws-architect.md`](../agents/senior-aws-architect.md)/[`../agents/senior-devops-engineer.md`](../agents/senior-devops-engineer.md), según corresponda).
- **Produce**: cambio implementado, medido con la misma metodología del paso 2.
- **Entrega a**: Senior Code Reviewer.
- **Terminada cuando**: la medición posterior alcanza el objetivo definido en la fase 1, con la misma metodología de medición que la línea base.

### 6. Senior Code Reviewer

- **Hace**: revisa que la optimización no introduce complejidad desproporcionada al beneficio medido (ver YAGNI, [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)) ni un anti-patrón nuevo.
- **Produce**: aprobación.

## Diagrama de flujo

```
Senior Business Analyst (cuantifica el objetivo)
        ↓
Senior Debugger + Senior Performance Engineer (mide, identifica el cuello de botella real)
        ↓
   ¿causa estructural?
        │
   No ──┴── Sí → Software Architect → Tech Lead
        ↓                                ↓
Agente técnico (según tecnología) ←─────┘
        ↓ (medido contra el mismo baseline)
Senior Code Reviewer
        ↓
   [Producción]
```

## Regla de oro de este playbook

Nunca se optimiza sin medición previa y posterior con la misma metodología — "parece más rápido" no es un resultado válido en este playbook.

# Latency and Throughput

> Ver [`performance-budget.md`](./performance-budget.md) para cómo estas métricas se convierten en requisitos verificables.

## Dos métricas distintas, con frecuencia confundidas

- **Latencia**: cuánto tarda **una** petición individual en completarse.
- **Throughput**: cuántas peticiones se procesan **en total** por unidad de tiempo.

Un sistema puede tener latencia individual alta pero throughput alto (procesa muchas peticiones en paralelo, cada una tarda pero hay muchas simultáneas) — o latencia baja con throughput bajo (cada petición es rápida, pero el sistema no procesa muchas a la vez). Optimizar una sin considerar la otra puede degradar la métrica que en realidad importaba para el caso de uso.

## Percentiles, nunca solo el promedio

```
Media: 120ms — parece aceptable
p50 (mediana): 80ms
p95: 450ms
p99: 3200ms — el 1% de usuarios tiene una experiencia radicalmente peor que "la media"
```

La media es sensible a la forma de la distribución y oculta sistemáticamente los peores casos — dos sistemas con la misma media pueden tener experiencias de usuario radicalmente distintas si uno tiene una cola larga de valores extremos (p99 muy alto) y el otro no. p95/p99 son los indicadores que reflejan la experiencia real de una fracción significativa de usuarios, no solo del caso típico.

## Ley de Little: relación entre latencia, throughput y concurrencia

```
L = λ × W
(número de peticiones en el sistema) = (throughput) × (latencia media)
```

Si el throughput crece sin que la latencia empeore, el número de peticiones concurrentes en el sistema (conexiones abiertas, threads ocupados) también crece proporcionalmente — útil para estimar cuántos recursos concurrentes (pool de conexiones, threads) se necesitan para un throughput objetivo dado, sin necesidad de medirlo empíricamente desde cero.

## El trade-off: throughput alto puede requerir aceptar latencia individual mayor

```
Batching: agrupar múltiples peticiones antes de procesarlas mejora throughput (menos overhead
por petición) a costa de latencia individual (cada petición espera a que se forme el batch)
```

Una decisión de diseño que prioriza throughput (batching, colas con procesamiento por lotes, ver [`../integration/message-queues.md`](../integration/message-queues.md)) casi siempre implica un trade-off explícito con la latencia individual — la elección correcta depende de qué métrica importa más para el caso de uso concreto (ver [`performance-budget.md`](./performance-budget.md)).

## Cuándo throughput importa más que latencia (y viceversa)

| Caso de uso | Métrica prioritaria |
|---|---|
| API interactiva consultada por un usuario esperando respuesta | Latencia (p95/p99) |
| Procesamiento batch de datos masivos overnight | Throughput total |
| Sistema de trading de alta frecuencia | Latencia (incluso p99.9) |
| Pipeline de ingesta de eventos de analítica | Throughput, latencia individual poco relevante |

## Checklist

- [ ] ¿Se reportan percentiles (p95/p99), no solo la media?
- [ ] ¿Se ha identificado explícitamente si el caso de uso prioriza latencia o throughput, antes de optimizar?
- [ ] ¿Se ha considerado la Ley de Little al dimensionar recursos concurrentes para un throughput objetivo?

Ver también: [`performance-budget.md`](./performance-budget.md), [`scalability.md`](./scalability.md), [`bottleneck-analysis.md`](./bottleneck-analysis.md).

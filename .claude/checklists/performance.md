# Checklist — Rendimiento

> Verificar antes de dar por terminada cualquier tarea de diagnóstico u optimización de rendimiento. Cada ítem enlaza al documento donde se explica el criterio en profundidad — esta checklist no repite esa teoría, solo la convierte en verificación accionable.

## Proceso (nunca optimizar por intuición)

- [ ] El problema está cuantificado en percentiles (p95/p99), con un objetivo explícito acordado, no "debe ir más rápido" — [`performance-budget.md`](../knowledge/performance/performance-budget.md)
- [ ] El cuello de botella se localizó con datos (profiling, plan de ejecución, wait stats, método USE), no por intuición — [`bottleneck-analysis.md`](../knowledge/performance/bottleneck-analysis.md)
- [ ] La hipótesis de causa es falsable, con un experimento concreto que la confirma o refuta — [`performance-engineering.md`](../knowledge/performance/performance-engineering.md)
- [ ] Se cambió una única variable por experimento, nunca varias simultáneamente — [`performance-engineering.md`](../knowledge/performance/performance-engineering.md)

## Medición

- [ ] Se reportan percentiles, no solo la media — [`latency-and-throughput.md`](../knowledge/performance/latency-and-throughput.md)
- [ ] Cualquier benchmark se ejecutó con múltiples repeticiones, excluyendo el periodo de warm-up — [`benchmarking.md`](../knowledge/performance/benchmarking.md)
- [ ] La mejora se validó con la misma metodología de medición (mismo entorno, mismas condiciones) que la línea base — [`performance-engineering.md`](../knowledge/performance/performance-engineering.md)

## Diagnóstico específico del stack

- [ ] Se consultó el documento de diagnóstico del stack afectado antes de asumir la causa — [`../knowledge/java/performance.md`](../knowledge/java/performance.md) / [`../knowledge/node/performance.md`](../knowledge/node/performance.md) / [`../knowledge/sqlserver/performance.md`](../knowledge/sqlserver/performance.md) / [`../knowledge/android/performance.md`](../knowledge/android/performance.md)

## Escalabilidad y capacidad

- [ ] Si la solución implica escalar, el servicio es genuinamente stateless o se ha evaluado explícitamente su capacidad de escalar — [`scalability.md`](../knowledge/performance/scalability.md)
- [ ] La decisión entre escalado horizontal y vertical está justificada, no asumida — [`../decisions/horizontal-vs-vertical-scaling.md`](../decisions/horizontal-vs-vertical-scaling.md)
- [ ] Si se introduce caché, se evaluó primero si existe una solución estructural al problema — [`caching-strategy.md`](../knowledge/performance/caching-strategy.md)

## Coste y validación final

- [ ] La inversión de esfuerzo está justificada por impacto de negocio cuantificable, no solo técnico — [`cost-vs-performance.md`](../knowledge/performance/cost-vs-performance.md)
- [ ] Se ha considerado si la mejora reduce coste y mejora rendimiento simultáneamente antes de asumir un trade-off — [`cost-vs-performance.md`](../knowledge/performance/cost-vs-performance.md)
- [ ] Existe un gate de regresión de rendimiento en el pipeline para prevenir que esta mejora se pierda silenciosamente en el futuro — [`performance-regression.md`](../knowledge/performance/performance-regression.md)

Ver también: [`../knowledge/performance/README.md`](../knowledge/performance/README.md), [`../playbooks/performance.md`](../playbooks/performance.md), [`../agents/senior-performance-engineer.md`](../agents/senior-performance-engineer.md).

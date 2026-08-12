# Knowledge — Performance

Índice del dominio **transversal** de ingeniería de rendimiento — metodología, criterio y toma de decisiones. Diagnóstico específico por tecnología (JVM, Event Loop, SQL Server, Android) **no se duplica aquí**, se enlaza desde cada documento correspondiente.

| Documento | Descripción |
|---|---|
| [`performance-engineering.md`](./performance-engineering.md) | El método: Medir → Localizar → Datos → Hipótesis → Experimento → Medición → Optimización → Validación |
| [`performance-budget.md`](./performance-budget.md) | Límites explícitos verificables, gates automatizados, presupuesto por percentil |
| [`capacity-planning.md`](./capacity-planning.md) | Proyección de demanda, conversión a capacidad, margen de seguridad |
| [`benchmarking.md`](./benchmarking.md) | Rigor estadístico, warm-up, reproducibilidad |
| [`bottleneck-analysis.md`](./bottleneck-analysis.md) | Método USE, Ley de Amdahl, contención de locks |
| [`latency-and-throughput.md`](./latency-and-throughput.md) | Percentiles, Ley de Little, trade-off latencia/throughput |
| [`scalability.md`](./scalability.md) | Requisito de statelessness, escalabilidad sub-lineal real |
| [`caching-strategy.md`](./caching-strategy.md) | Patrones cache-aside/write-through/write-behind, invalidación |
| [`load-testing-types.md`](./load-testing-types.md) | Load vs. Stress vs. Spike vs. Soak testing |
| [`performance-regression.md`](./performance-regression.md) | Detección continua, línea base, bisección |
| [`cost-vs-performance.md`](./cost-vs-performance.md) | Rendimientos decrecientes, impacto de negocio cuantificado |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes del dominio |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos de rendimiento |
| [`examples.md`](./examples.md) | Casos de uso completos |

## Dominios relacionados (no duplicados aquí)

| Tema | Dónde vive |
|---|---|
| Profiling/GC de la JVM | [`../java/performance.md`](../java/performance.md) |
| Event Loop, Worker Threads de Node.js | [`../node/performance.md`](../node/performance.md) |
| Execution plans, índices, wait stats de SQL Server | [`../sqlserver/performance.md`](../sqlserver/performance.md) |
| Hilo principal, recomposición de Android/Compose | [`../android/performance.md`](../android/performance.md) |
| Observabilidad de plataforma (métricas, dashboards) | [`../devops/observability.md`](../devops/observability.md), [`../devops/monitoring.md`](../devops/monitoring.md) |
| Metodología general de diagnóstico | [`../../prompts/performance.md`](../../prompts/performance.md) |
| Proceso end-to-end de un problema de rendimiento | [`../../playbooks/performance.md`](../../playbooks/performance.md) |

Ver [`../../decisions/horizontal-vs-vertical-scaling.md`](../../decisions/horizontal-vs-vertical-scaling.md) para la comparativa de decisión asociada.

Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí ni en los dominios relacionados (regla de oro nº2).

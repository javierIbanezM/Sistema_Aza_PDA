# Best Practices (Performance)

> Síntesis de las reglas más importantes del dominio transversal de rendimiento. Para diagnóstico específico por stack, ver `knowledge/java/performance.md`, `knowledge/node/performance.md`, `knowledge/sqlserver/performance.md`, `knowledge/android/performance.md` — no se duplican aquí.

## Método

- Nunca optimizar por intuición: Medir → Localizar cuello de botella → Datos → Hipótesis → Experimento → Medición → Optimización → Validación (ver [`performance-engineering.md`](./performance-engineering.md)).
- Cambiar una única variable por experimento, nunca varias simultáneamente.
- Validar la mejora con la misma metodología de medición que la línea base.

## Cuantificación

- Todo requisito de rendimiento en percentiles (p95/p99) y con performance budget explícito, nunca "debe ser rápido" (ver [`performance-budget.md`](./performance-budget.md), [`latency-and-throughput.md`](./latency-and-throughput.md)).
- Presupuestos verificados automáticamente en el pipeline como gate, no solo revisados manualmente.

## Diagnóstico

- Método USE (Utilization/Saturation/Errors) recorrido sistemáticamente, nunca solo el recurso que "parece" sospechoso (ver [`bottleneck-analysis.md`](./bottleneck-analysis.md)).
- Contención de locks/concurrencia descartada explícitamente, no invisible en métricas de recursos estándar.

## Benchmarking y testing de carga

- Múltiples ejecuciones con percentiles reportados, warm-up excluido, condiciones reproducibles (ver [`benchmarking.md`](./benchmarking.md)).
- Tipo de prueba (Load/Stress/Spike/Soak) elegido según la pregunta específica a responder (ver [`load-testing-types.md`](./load-testing-types.md)).

## Escalabilidad y capacidad

- Servicios genuinamente stateless para permitir escalado horizontal sin afinidad de sesión (ver [`scalability.md`](./scalability.md)).
- Capacidad proyectada desde datos históricos + crecimiento + eventos conocidos, con margen de seguridad justificado (ver [`capacity-planning.md`](./capacity-planning.md)).

## Coste

- Toda mejora de rendimiento conectada a impacto de negocio cuantificable antes de invertir esfuerzo desproporcionado (ver [`cost-vs-performance.md`](./cost-vs-performance.md)).

Ver [`pitfalls.md`](./pitfalls.md) y [`examples.md`](./examples.md).

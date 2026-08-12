# Examples (Performance)

## Ejemplo 1 — Diagnóstico completo de una API lenta con metodología completa

**Contexto**: `/api/pedidos` reporta p95 de 2.5s, objetivo de negocio es 300ms.

**Flujo aplicado**: se cuantifica el problema (ver [`performance-budget.md`](./performance-budget.md)) — objetivo explícito p95 < 300ms con 500 usuarios concurrentes. Se aplica el método USE (ver [`bottleneck-analysis.md`](./bottleneck-analysis.md)): CPU normal, memoria normal, I/O de base de datos con alta saturación. Se confirma con `knowledge/sqlserver/execution-plans.md` — query N+1 real. Hipótesis: "el 80% de la latencia proviene de N+1 en carga de líneas de pedido". Experimento: añadir `JOIN FETCH` (una única variable). Medición: p95 baja a 220ms. Validación: benchmark completo con la misma metodología que la línea base (ver [`benchmarking.md`](./benchmarking.md)) confirma la mejora sostenida bajo carga real, no solo en una prueba puntual.

---

## Ejemplo 2 — Capacity planning para un evento de marketing conocido

**Contexto**: campaña de marketing prevista en 6 semanas, se espera 8x el tráfico normal.

**Flujo aplicado**: se proyecta la demanda (ver [`capacity-planning.md`](./capacity-planning.md)) combinando tráfico histórico + el factor 8x conocido. Se ejecuta un Load Test (ver [`load-testing-types.md`](./load-testing-types.md)) simulando esa carga proyectada contra staging, confirmando que el sistema cumple el performance budget a ese volumen. Se ejecuta adicionalmente un Spike Test para verificar que el autoescalado reacciona a tiempo ante el patrón real esperado (pico repentino al inicio de la campaña, no incremento gradual). Se dimensiona la capacidad base con margen de seguridad del 30%, coordinando con [`../../agents/senior-aws-architect.md`](../../agents/senior-aws-architect.md) para el aprovisionamiento.

---

## Ejemplo 3 — Detección de una regresión de rendimiento tras varias releases

**Contexto**: el equipo nota que el sistema "se siente más lento" que hace unos meses, sin un incidente puntual identificable.

**Flujo aplicado**, siguiendo [`performance-regression.md`](./performance-regression.md): se confirma con datos históricos de monitorización (ver [`../devops/monitoring.md`](../devops/monitoring.md)) que efectivamente hay degradación gradual acumulada del 40% en 6 meses, sin un único cambio dramático. Se usa `git bisect` combinado con un script de benchmark automatizado para localizar en qué commits se introdujo cada incremento significativo. Se aplica [`bottleneck-analysis.md`](./bottleneck-analysis.md) a cada commit identificado, encontrando que varias dependencias añadidas incrementaron el tiempo de arranque y una nueva funcionalidad introdujo una query no optimizada. Se corrige cada causa raíz específica, estableciendo un gate de regresión automatizado (ver [`performance-regression.md`](./performance-regression.md)) para prevenir que se repita silenciosamente.

---

## Ejemplo 4 — Decisión de caché con trade-off de coste explícito

**Contexto**: un endpoint de catálogo de productos genera carga significativa en la base de datos.

**Flujo aplicado**: se evalúa con [`caching-strategy.md`](./caching-strategy.md) — alta relación lectura/escritura (el catálogo cambia pocas veces al día, se consulta miles de veces), buen candidato. Se elige patrón cache-aside con TTL de 1 hora + invalidación activa ante actualización de producto. Se cuantifica el beneficio con [`cost-vs-performance.md`](./cost-vs-performance.md): reduce carga de base de datos permitiendo mantener el tier actual de RDS en lugar de escalar verticalmente (ahorro directo de coste), y mejora p95 del endpoint de 400ms a 40ms — una optimización que reduce coste y mejora rendimiento simultáneamente, priorizada sin necesidad de justificar un trade-off.

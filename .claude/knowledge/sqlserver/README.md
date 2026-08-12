# Knowledge — SQL Server

Índice de la documentación profunda de SQL Server. Los patrones de diseño y anti-patrones (incluido N+1 Query, SQL Injection) son globales, en [`../../patterns/`](../../patterns/) y [`../../anti-patterns/`](../../anti-patterns/).

| Documento | Descripción |
|---|---|
| [`engine.md`](./engine.md) | Arquitectura interna: páginas, filegroups, modelos de recuperación, niveles de aislamiento |
| [`execution-plans.md`](./execution-plans.md) | Cómo leer e interpretar planes de ejecución, operadores, plan cache, parameter sniffing |
| [`indexes.md`](./indexes.md) | Clustered/non-clustered, columnas INCLUDE, regla ESR, fragmentación, coste de mantenerlos |
| [`statistics.md`](./statistics.md) | Qué son, actualización automática/manual, relación con parameter sniffing |
| [`blocking.md`](./blocking.md) | Diagnóstico, `READ COMMITTED SNAPSHOT`, transacciones cortas, `NOLOCK` |
| [`deadlocks.md`](./deadlocks.md) | Detección, deadlock graph, prevención por orden consistente, reintento en aplicación |
| [`tempdb.md`](./tempdb.md) | Qué se almacena, múltiples archivos de datos, dimensionamiento, diagnóstico de contención |
| [`partitioning.md`](./partitioning.md) | Cuándo particionar, partition elimination, `SWITCH PARTITION` para purga |
| [`backup.md`](./backup.md) | Full/Differential/Log, RPO, verificación de integridad, almacenamiento externo |
| [`restore.md`](./restore.md) | Restauración point-in-time, verificación de cadena, RTO medido con simulacros |
| [`jobs.md`](./jobs.md) | SQL Server Agent: steps, schedules, mantenimiento automatizado, notificación de fallo |
| [`security.md`](./security.md) | Menor privilegio, prevención de SQL Injection, Row-Level Security, cifrado, auditoría |
| [`replication.md`](./replication.md) | Tipos de replicación, Always On Availability Groups, cuándo NO replicar |
| [`performance.md`](./performance.md) | SARGable, conversiones implícitas, batching, wait statistics |
| [`examples.md`](./examples.md) | Casos de uso completos combinando varios documentos |

## Cómo usar este índice

- Para el mapeo objeto-relacional desde Java, ver [`../hibernate/`](../hibernate/) y [`../spring/spring-data-jpa.md`](../spring/spring-data-jpa.md) — este dominio cubre SQL Server como motor, independiente del lenguaje/ORM que lo consuma.
- Para SQL Injection, ver el anti-patrón global [`../../anti-patterns/sql-injection.md`](../../anti-patterns/sql-injection.md) — no duplicado aquí, solo referenciado desde [`security.md`](./security.md).
- Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí (regla de oro nº2).

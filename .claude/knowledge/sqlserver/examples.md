# Examples (SQL Server)

Casos de uso completos que combinan varios documentos de este dominio.

---

## Ejemplo 1 — Diagnóstico completo de una query lenta reportada por soporte

**Contexto**: `GET /api/v1/pedidos?clienteId=c-123` tarda varios segundos en un cliente con historial extenso.

**Flujo aplicado**:

1. Obtener el plan real con `SET STATISTICS XML/IO/TIME ON` — ver [`execution-plans.md`](./execution-plans.md).
2. El plan muestra un `Clustered Index Scan` sobre `Pedidos` en lugar de un `Seek` — se verifica que no existe índice sobre `ClienteId`, o que la condición no es SARGable (ver [`performance.md`](./performance.md)).
3. Se verifica la fecha de última actualización de estadísticas con `DBCC SHOW_STATISTICS` — ver [`statistics.md`](./statistics.md); si están desactualizadas tras una carga masiva reciente, se actualizan primero antes de sacar conclusiones sobre el índice.
4. Se crea un índice `IX_Pedidos_ClienteId` con columnas `INCLUDE` para las columnas realmente consultadas — ver [`indexes.md`](./indexes.md), usando la recipe [`../../recipes/sqlserver-create-index.md`](../../recipes/sqlserver-create-index.md).
5. Se vuelve a medir con la misma metodología: el plan pasa a `Index Seek`, `logical reads` baja drásticamente, tiempo de respuesta aceptable.

---

## Ejemplo 2 — Resolver blocking recurrente en horario de alta concurrencia

**Contexto**: durante las horas pico, varias transacciones de confirmación de pedido quedan bloqueadas esperando varios segundos.

**Flujo aplicado**:

1. Diagnosticar con `sys.dm_exec_requests`/`sp_who2` durante el pico — ver [`blocking.md`](./blocking.md).
2. Se identifica que una transacción de reporte con `SELECT` largo mantiene bloqueos de lectura que colisionan con las escrituras de confirmación de pedidos.
3. Se evalúa y habilita `READ COMMITTED SNAPSHOT` — ver [`blocking.md`](./blocking.md) y su impacto en [`tempdb.md`](./tempdb.md) (se dimensiona TempDB con más archivos/espacio antes de habilitarlo en producción).
4. Se verifica que las transacciones de confirmación de pedido no realizan I/O externo dentro de la transacción (ver [`blocking.md`](./blocking.md)) — se encuentra una llamada a un servicio de notificación dentro de la transacción y se mueve fuera, usando el patrón de evento tras commit (ver [`../spring/spring-events.md`](../spring/spring-events.md), `@TransactionalEventListener(phase = AFTER_COMMIT)`).
5. Blocking se reduce drásticamente tras ambos cambios, verificado con la misma consulta de diagnóstico.

---

## Ejemplo 3 — Diseñar la estrategia de backup/restore para un sistema crítico

**Contexto**: nuevo sistema de pedidos en producción, RPO objetivo de 15 minutos, RTO objetivo de 1 hora.

**Flujo aplicado**:

1. Se elige modelo de recuperación `FULL` (ver [`engine.md`](./engine.md)), único que permite backups de log frecuentes.
2. Se configura Full semanal + Differential diario + Log cada 15 minutos — ver [`backup.md`](./backup.md), cumpliendo el RPO de 15 minutos.
3. Se automatiza como jobs de SQL Server Agent con notificación de fallo — ver [`jobs.md`](./jobs.md).
4. Se documenta y ejecuta un simulacro de restauración point-in-time completo en un entorno separado — ver [`restore.md`](./restore.md) — midiendo el tiempo real, confirmando que el RTO de 1 hora es alcanzable.
5. Se configuran backups replicados a almacenamiento externo con copia offline, mitigando el riesgo de ransomware.

---

## Ejemplo 4 — Purga de datos históricos sin impacto en producción

**Contexto**: la tabla `Pedidos` acumula varios años de histórico; el negocio pide purgar/archivar datos de más de 2 años sin afectar el rendimiento del sistema en producción.

**Flujo aplicado**:

1. Se evalúa el volumen (decenas de millones de filas) y el patrón de purga recurrente por fecha — se decide particionar por año, ver [`partitioning.md`](./partitioning.md) (no se habría justificado con volumen menor, ver YAGNI en [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)).
2. Se crea función y esquema de partición por año, se migra la tabla (proceso planificado con ventana de mantenimiento, ver [`../architecture/documentacion-tecnica.md`](../architecture/technical-documentation.md) para el ADR de esta decisión).
3. La purga periódica se implementa con `ALTER TABLE ... SWITCH PARTITION` hacia una tabla de archivo — operación de metadatos casi instantánea, sin `DELETE` masivo ni impacto de bloqueo (ver [`partitioning.md`](./partitioning.md) y [`performance.md`](./performance.md) sobre batching como alternativa si no se particiona).
4. Se automatiza como job mensual — ver [`jobs.md`](./jobs.md).

# Performance (SQL Server)

> Ver [`execution-plans.md`](./execution-plans.md), [`indexes.md`](./indexes.md) y [`statistics.md`](./statistics.md) para el diagnóstico detallado de queries individuales — este documento cubre el diagnóstico de rendimiento a nivel de instancia/carga general. Ver también [`../../prompts/performance.md`](../../prompts/performance.md) para la metodología general de "medir antes de optimizar".

## SARGable vs. non-SARGable: el error más común

Una condición **SARGable** (Search ARGument-able) permite al optimizador usar un índice mediante un Seek; una condición non-SARGable fuerza un Scan aunque exista un índice apropiado.

```sql
-- Non-SARGable: la función sobre la COLUMNA impide usar el índice de FechaCreacion
SELECT * FROM Pedidos WHERE YEAR(FechaCreacion) = 2026;

-- SARGable: la comparación es directa sobre la columna, el índice se puede usar
SELECT * FROM Pedidos WHERE FechaCreacion >= '2026-01-01' AND FechaCreacion < '2027-01-01';
```

```sql
-- Non-SARGable: wildcard al inicio del LIKE impide usar el índice
SELECT * FROM Clientes WHERE Nombre LIKE '%García';

-- SARGable: wildcard solo al final SÍ puede usar el índice
SELECT * FROM Clientes WHERE Nombre LIKE 'García%';
```

Regla general: cualquier función, cálculo o conversión de tipo aplicada **a la columna** (no al valor de comparación) en la cláusula `WHERE` vuelve la condición non-SARGable. Reescribir la condición para aplicar la transformación al valor literal/parámetro en su lugar, dejando la columna "limpia".

## Conversiones implícitas de tipo

```sql
-- Columna ClienteId es VARCHAR, pero se compara con un literal NVARCHAR (Java/C# suelen enviar NVARCHAR por defecto)
SELECT * FROM Pedidos WHERE ClienteId = N'c-123'; -- conversión implícita, puede impedir el uso eficiente del índice
```

Verificar en el plan de ejecución (ver [`execution-plans.md`](./execution-plans.md)) el warning de conversión implícita — asegurar que el tipo de dato del parámetro enviado desde la aplicación coincide exactamente con el tipo de la columna en la base de datos.

## `SELECT *`: evitar en código de producción

```sql
-- Evitar: trae todas las columnas, incluidas las no necesarias, impide covering indexes (ver indexes.md)
SELECT * FROM Pedidos WHERE ClienteId = 'c-123';

-- Preferir: solo las columnas necesarias
SELECT Id, Estado, Total FROM Pedidos WHERE ClienteId = 'c-123';
```

Además de transferir datos innecesarios, `SELECT *` impide que un índice `INCLUDE` (ver [`indexes.md`](./indexes.md)) cubra completamente la query — cualquier columna no anticipada fuerza un Key Lookup.

## Batching de operaciones masivas

```sql
-- Evitar: UPDATE masivo de una sola vez sobre millones de filas — bloquea, genera log de transacciones enorme
UPDATE Pedidos SET Estado = 'ARCHIVADO' WHERE FechaCreacion < '2020-01-01';

-- Preferir: batches pequeños con pausa entre ellos
WHILE 1 = 1
BEGIN
    UPDATE TOP (5000) Pedidos SET Estado = 'ARCHIVADO'
    WHERE FechaCreacion < '2020-01-01' AND Estado <> 'ARCHIVADO';

    IF @@ROWCOUNT = 0 BREAK;
    WAITFOR DELAY '00:00:00.5'; -- pausa breve, permite que otras transacciones intercalen
END
```

Ver [`blocking.md`](./blocking.md) — operaciones masivas sin batching son la causa más común de lock escalation y bloqueo prolongado sobre tablas activas.

## Wait Statistics: dónde está esperando realmente el sistema

```sql
SELECT wait_type, wait_time_ms, waiting_tasks_count
FROM sys.dm_os_wait_stats
WHERE wait_type NOT IN ('SLEEP_TASK', 'BROKER_TASK_STOP', 'CLR_SEMAPHORE') -- filtrar esperas benignas del sistema
ORDER BY wait_time_ms DESC;
```

| Wait type frecuente | Indica |
|---|---|
| `PAGEIOLATCH_*` | Espera de I/O de disco — almacenamiento lento o insuficiente memoria de buffer pool |
| `LCK_M_*` | Espera de bloqueos — ver [`blocking.md`](./blocking.md) |
| `CXPACKET`/`CXCONSUMER` | Consultas en paralelo con desequilibrio entre hilos — revisar `MAXDOP` y el diseño de la query |
| `PAGELATCH_*` | Contención en páginas de asignación, frecuente en TempDB — ver [`tempdb.md`](./tempdb.md) |
| `RESOURCE_SEMAPHORE` | Presión de memoria para ejecutar queries (especialmente `Sort`/`Hash Match` grandes) |

Las estadísticas de espera acumuladas desde el arranque de la instancia dan una visión de **dónde pasa el tiempo el motor en agregado** — el punto de partida correcto para priorizar el esfuerzo de optimización, en lugar de optimizar la primera query lenta que se encuentra sin saber si es representativa del problema real.

## Checklist

- [ ] ¿Las condiciones `WHERE` son SARGables (sin funciones sobre la columna)?
- [ ] ¿Se ha verificado ausencia de conversiones implícitas de tipo en joins/filtros frecuentes?
- [ ] ¿Se evita `SELECT *` en queries de producción?
- [ ] ¿Las operaciones masivas usan batching en lugar de una única transacción gigante?
- [ ] ¿Se ha revisado `sys.dm_os_wait_stats` para priorizar el esfuerzo de optimización según el cuello de botella real de la instancia?

Ver también: [`execution-plans.md`](./execution-plans.md), [`indexes.md`](./indexes.md), [`blocking.md`](./blocking.md), [`../../prompts/performance.md`](../../prompts/performance.md).

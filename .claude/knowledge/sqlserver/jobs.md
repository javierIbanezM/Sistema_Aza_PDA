# Jobs (SQL Server Agent)

> Ver [`indexes.md`](./indexes.md) (mantenimiento de índices), [`statistics.md`](./statistics.md) (actualización de estadísticas) y [`backup.md`](./backup.md) — los tres se automatizan típicamente como jobs.

## Estructura de un job

Un job de SQL Server Agent se compone de uno o más **steps** (pasos, ejecutados secuencialmente o con lógica condicional de éxito/fallo entre ellos) y un **schedule** (programación: frecuencia, ventana horaria).

```sql
USE msdb;
GO

EXEC sp_add_job
    @job_name = N'Mantenimiento_Indices_Pedidos',
    @enabled = 1,
    @description = N'Reorganiza/reconstruye índices de la tabla Pedidos según fragmentación';

EXEC sp_add_jobstep
    @job_name = N'Mantenimiento_Indices_Pedidos',
    @step_name = N'Ejecutar mantenimiento',
    @subsystem = N'TSQL',
    @command = N'EXEC dbo.sp_MantenimientoIndices @TablaPrefijo = ''Pedidos''',
    @on_success_action = 3, -- ir al siguiente step
    @on_fail_action = 2;    -- terminar el job reportando fallo

EXEC sp_add_schedule
    @schedule_name = N'Diario_03AM',
    @freq_type = 4,        -- diario
    @freq_interval = 1,
    @active_start_time = 030000;

EXEC sp_attach_schedule
    @job_name = N'Mantenimiento_Indices_Pedidos',
    @schedule_name = N'Diario_03AM';

EXEC sp_add_jobserver
    @job_name = N'Mantenimiento_Indices_Pedidos',
    @server_name = N'(local)';
```

## Job de mantenimiento de índices basado en fragmentación real

```sql
CREATE PROCEDURE dbo.sp_MantenimientoIndices
    @TablaPrefijo NVARCHAR(100) = NULL
AS
BEGIN
    SET NOCOUNT ON;

    DECLARE @sql NVARCHAR(MAX);

    DECLARE cur CURSOR FOR
        SELECT
            'ALTER INDEX ' + QUOTENAME(i.name) + ' ON ' + QUOTENAME(OBJECT_NAME(ips.object_id)) +
            CASE WHEN ips.avg_fragmentation_in_percent > 30 THEN ' REBUILD;' ELSE ' REORGANIZE;' END
        FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
        JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
        WHERE ips.avg_fragmentation_in_percent > 10
          AND i.name IS NOT NULL
          AND (@TablaPrefijo IS NULL OR OBJECT_NAME(ips.object_id) LIKE @TablaPrefijo + '%');

    OPEN cur;
    FETCH NEXT FROM cur INTO @sql;
    WHILE @@FETCH_STATUS = 0
    BEGIN
        EXEC sp_executesql @sql;
        FETCH NEXT FROM cur INTO @sql;
    END
    CLOSE cur; DEALLOCATE cur;
END;
```

Ver [`indexes.md`](./indexes.md) para el razonamiento de los umbrales 10%/30% (REORGANIZE vs. REBUILD).

## Monitorización y alertas de fallo de job

```sql
-- Configurar notificación por email al operador ante fallo del job
EXEC sp_update_job
    @job_name = N'Mantenimiento_Indices_Pedidos',
    @notify_level_email = 2, -- notificar solo si falla
    @notify_email_operator_name = N'DBA_OnCall';
```

Un job de mantenimiento crítico (backups, particularmente) que falla silenciosamente sin alertar es un riesgo grave — verificar siempre que la notificación de fallo está configurada, no solo que el job existe y está habilitado.

## Ver historial de ejecución

```sql
SELECT
    j.name AS Job,
    h.run_date, h.run_time, h.run_duration,
    CASE h.run_status WHEN 0 THEN 'Fallido' WHEN 1 THEN 'Exitoso' WHEN 3 THEN 'Cancelado' END AS Resultado,
    h.message
FROM msdb.dbo.sysjobhistory h
JOIN msdb.dbo.sysjobs j ON h.job_id = j.job_id
WHERE j.name = N'Mantenimiento_Indices_Pedidos'
ORDER BY h.run_date DESC, h.run_time DESC;
```

## Checklist

- [ ] ¿Los jobs críticos (backup, mantenimiento de índices, actualización de estadísticas) tienen notificación de fallo configurada?
- [ ] ¿El mantenimiento de índices se basa en fragmentación real medida, no en un `REBUILD` incondicional de todos los índices?
- [ ] ¿Se ha verificado el historial de ejecución periódicamente, no solo asumido que "si no hay alertas, todo va bien"?
- [ ] ¿La ventana de ejecución de jobs pesados (mantenimiento, backups completos) evita las horas de mayor carga transaccional?

Ver también: [`indexes.md`](./indexes.md), [`statistics.md`](./statistics.md), [`backup.md`](./backup.md).

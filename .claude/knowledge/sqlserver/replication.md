# Replication

> Ver [`backup.md`](./backup.md)/[`restore.md`](./restore.md) para las estrategias de continuidad complementarias (backup/restore resuelve recuperación ante desastres puntual; replicación resuelve disponibilidad/distribución continua).

## Tipos de replicación y cuándo usar cada uno

| Tipo | Comportamiento | Uso típico |
|---|---|---|
| **Transaccional** | Cambios replicados casi en tiempo real, en el orden exacto en que ocurrieron | Distribuir carga de lectura (réplicas de solo lectura), sincronizar un datawarehouse casi en tiempo real |
| **Merge** | Permite cambios en múltiples nodos, con resolución de conflictos | Escenarios offline/multi-maestro (sucursales que sincronizan periódicamente con conectividad intermitente) |
| **Snapshot** | Copia completa periódica, no incremental | Datos de referencia que cambian poco, distribución inicial de otros tipos de replicación |

La réplica transaccional es la más común en sistemas OLTP modernos; merge replication es cada vez menos usada, generalmente reemplazada por patrones de sincronización a nivel de aplicación (Event Sourcing, colas de mensajería, ver `CLAUDE.md` sección 16) cuando el escenario lo permite.

## Always On Availability Groups (alternativa moderna preferida)

Para alta disponibilidad y distribución de lecturas en SQL Server moderno, **Always On Availability Groups** es generalmente preferible a la replicación transaccional clásica:

```sql
CREATE AVAILABILITY GROUP AG_Pedidos
FOR DATABASE PedidosDB
REPLICA ON
    'NODO1' WITH (ENDPOINT_URL = 'TCP://nodo1:5022', AVAILABILITY_MODE = SYNCHRONOUS_COMMIT, FAILOVER_MODE = AUTOMATIC),
    'NODO2' WITH (ENDPOINT_URL = 'TCP://nodo2:5022', AVAILABILITY_MODE = SYNCHRONOUS_COMMIT, FAILOVER_MODE = AUTOMATIC),
    'NODO3' WITH (ENDPOINT_URL = 'TCP://nodo3:5022', AVAILABILITY_MODE = ASYNCHRONOUS_COMMIT, FAILOVER_MODE = MANUAL);
```

- **Modo síncrono**: la transacción no confirma hasta que la réplica secundaria confirma también — cero pérdida de datos ante failover, a costa de latencia adicional en cada escritura.
- **Modo asíncrono**: la primaria confirma sin esperar a la secundaria — menor latencia, con riesgo de pérdida de las últimas transacciones no replicadas aún en caso de failover.
- **Réplicas legibles**: las secundarias pueden atender tráfico de solo lectura (reportes, dashboards), descargando la primaria del tráfico de lectura que no necesita los datos más recientes al milisegundo.

## Latencia de replicación: monitorización

```sql
SELECT
    ag.name AS GrupoDisponibilidad,
    ar.replica_server_name,
    drs.last_hardened_time,
    drs.log_send_queue_size,
    drs.redo_queue_size
FROM sys.dm_hadr_database_replica_states drs
JOIN sys.availability_replicas ar ON drs.replica_id = ar.replica_id
JOIN sys.availability_groups ag ON ar.group_id = ag.group_id;
```

`log_send_queue_size`/`redo_queue_size` crecientes indican que la réplica secundaria no consigue mantenerse al día con la primaria — señal de que el modo asíncrono está acumulando retraso, o que la secundaria tiene un cuello de botella de I/O/CPU propio.

## Cuándo NO replicar (evaluar alternativas)

- Si el objetivo real es solo "tener un backup reciente", backup/restore periódico (ver [`backup.md`](./backup.md)) puede ser suficiente y mucho más simple operativamente que mantener infraestructura de replicación continua.
- Si el objetivo es desacoplar sistemas (que un cambio en `Pedidos` sea visible para otro servicio), un patrón de eventos de dominio (ver [`../architecture/ddd.md`](../architecture/ddd.md) y `CLAUDE.md` sección 16) suele ser más apropiado que replicar la base de datos completa a nivel de motor — replicar a nivel de base de datos acopla fuertemente el esquema interno de un servicio a sus consumidores.

## Checklist

- [ ] ¿Se ha evaluado Always On Availability Groups antes de configurar replicación transaccional clásica para alta disponibilidad?
- [ ] ¿El modo síncrono/asíncrono está elegido conscientemente según la tolerancia real a pérdida de datos vs. latencia?
- [ ] ¿Existe monitorización activa de la latencia de replicación (`log_send_queue_size`/`redo_queue_size`)?
- [ ] ¿Se ha descartado que el objetivo real sería mejor resuelto con backup/restore periódico o con eventos de dominio, en lugar de replicación continua de la base de datos completa?

Ver también: [`backup.md`](./backup.md), [`restore.md`](./restore.md), `CLAUDE.md` sección 16 y 17.

# Blocking

> Ver [`engine.md`](./engine.md) para los niveles de aislamiento que determinan el comportamiento de bloqueo, y [`deadlocks.md`](./deadlocks.md) para el caso específico de bloqueo circular irresoluble.

## Qué es blocking (y por qué no siempre es un problema)

Blocking ocurre cuando una transacción mantiene un bloqueo sobre un recurso (fila, página, tabla) que otra transacción necesita, obligando a la segunda a esperar. Es un comportamiento **normal y necesario** para garantizar consistencia — el problema no es que exista blocking, sino cuando es **excesivo, prolongado o evitable**.

## Diagnosticar blocking activo

```sql
-- Vista rápida de sesiones bloqueadas y qué las bloquea
SELECT
    blocking.session_id AS SesionBloqueadora,
    blocked.session_id AS SesionBloqueada,
    blocked.wait_type,
    blocked.wait_time,
    blocked_text.text AS QueryBloqueada
FROM sys.dm_exec_requests blocked
JOIN sys.dm_exec_sessions blocking ON blocked.blocking_session_id = blocking.session_id
CROSS APPLY sys.dm_exec_sql_text(blocked.sql_handle) blocked_text
WHERE blocked.blocking_session_id <> 0;
```

```sql
-- Detalle de todas las sesiones activas y qué esperan
EXEC sp_who2; -- vista rápida clásica; sys.dm_exec_requests es más detallada para diagnóstico moderno
```

## Tipos de bloqueo (granularidad)

| Nivel | Cuándo se escala aquí |
|---|---|
| Fila (RID/Key) | Operación afecta a pocas filas — el nivel más granular, menor contención |
| Página | Varias filas de la misma página modificadas en la misma transacción |
| Tabla | Operaciones masivas, o escalamiento de bloqueos (lock escalation) tras superar un umbral de bloqueos de fila/página |

**Lock escalation**: SQL Server escala automáticamente de bloqueos de fila/página a bloqueo de tabla completa cuando una transacción acumula muchos bloqueos individuales (~5000 por defecto), para reducir el overhead de memoria de gestionar tantos bloqueos granulares — a costa de bloquear la tabla entera para otras transacciones concurrentes. Transacciones muy grandes (UPDATE/DELETE masivos sin batching) son la causa típica.

## Reducir blocking: técnicas concretas

### `READ COMMITTED SNAPSHOT` (row versioning)

```sql
ALTER DATABASE PedidosDB SET READ_COMMITTED_SNAPSHOT ON;
```

Los lectores ya no bloquean a los escritores ni viceversa — leen una versión anterior consistente de la fila desde tempdb mientras la escritura está en curso. Es el cambio de mayor impacto para reducir blocking en sistemas OLTP con alta concurrencia lectura/escritura. Coste: mayor uso de tempdb (ver [`tempdb.md`](./tempdb.md)).

### Transacciones cortas

```sql
-- Evitar: transacción larga que mantiene bloqueos mientras espera I/O externo
BEGIN TRAN;
    UPDATE Pedidos SET Estado = 'PROCESANDO' WHERE Id = @id;
    -- llamada a un servicio externo aquí, dentro de la transacción — mal
    EXEC LlamarServicioExterno; 
    UPDATE Pedidos SET Estado = 'CONFIRMADO' WHERE Id = @id;
COMMIT;
```

Nunca realizar operaciones lentas (llamadas de red, procesamiento externo) **dentro** de una transacción de base de datos — cada milisegundo adicional dentro de la transacción es tiempo adicional que los bloqueos se mantienen, bloqueando a otras sesiones. Mover la lógica externa fuera de la transacción, o dividir en transacciones más pequeñas con un patrón de estado intermedio (ver [`../architecture/refactoring.md`](../architecture/refactoring.md)).

### Orden consistente de acceso a tablas

Acceder a las tablas siempre en el mismo orden en toda transacción multi-tabla reduce tanto blocking prolongado como el riesgo de deadlocks (ver [`deadlocks.md`](./deadlocks.md)).

### `NOLOCK` — usar con conocimiento explícito del riesgo

```sql
SELECT * FROM Pedidos WITH (NOLOCK) WHERE ClienteId = 'c-123';
```

Equivalente a `READ UNCOMMITTED` a nivel de tabla — elimina el bloqueo de lectura, pero permite leer datos no confirmados (que podrían revertirse) o incluso, en casos raros, filas duplicadas/perdidas durante un `Index Scan` concurrente con modificaciones. Apropiado solo para reportes/dashboards donde una inconsistencia transitoria es aceptable — nunca en lógica transaccional donde la exactitud importa. `READ COMMITTED SNAPSHOT` es preferible en la mayoría de casos donde la motivación es evitar bloqueo, ya que no sacrifica consistencia.

## Checklist

- [ ] ¿Se ha diagnosticado con `sys.dm_exec_requests` antes de asumir la causa del blocking?
- [ ] ¿Las transacciones evitan operaciones lentas (I/O externo) dentro de su alcance?
- [ ] ¿Se ha evaluado `READ COMMITTED SNAPSHOT` para el sistema, si sufre blocking recurrente lector-escritor?
- [ ] ¿El uso de `NOLOCK` está limitado a contextos donde la inconsistencia transitoria es explícitamente aceptable?

Ver también: [`deadlocks.md`](./deadlocks.md), [`engine.md`](./engine.md), [`tempdb.md`](./tempdb.md).

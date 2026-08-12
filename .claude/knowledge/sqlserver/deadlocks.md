# Deadlocks

> Ver [`blocking.md`](./blocking.md) para el bloqueo normal (temporal, se resuelve solo). Un deadlock es distinto: un ciclo de espera que **nunca** se resolvería solo — SQL Server lo detecta y lo rompe automáticamente eligiendo una víctima.

## Qué es un deadlock

Ocurre cuando dos (o más) transacciones se bloquean mutuamente en un ciclo: la Transacción A espera un recurso bloqueado por B, mientras B espera un recurso bloqueado por A. Ninguna puede continuar. SQL Server detecta este ciclo automáticamente (cada ~5 segundos por defecto, mediante el *deadlock monitor*) y termina una de las dos transacciones (la "víctima", normalmente la de menor coste de rollback) con el error 1205.

## Ejemplo clásico de deadlock

```sql
-- Sesión 1
BEGIN TRAN;
UPDATE Pedidos SET Estado = 'CONFIRMADO' WHERE Id = 'p-1'; -- bloquea Pedidos:p-1
-- ... tiempo ...
UPDATE Clientes SET UltimaCompra = GETDATE() WHERE Id = 'c-1'; -- espera el bloqueo de Clientes:c-1

-- Sesión 2 (concurrente)
BEGIN TRAN;
UPDATE Clientes SET UltimaCompra = GETDATE() WHERE Id = 'c-1'; -- bloquea Clientes:c-1
-- ... tiempo ...
UPDATE Pedidos SET Estado = 'CONFIRMADO' WHERE Id = 'p-1'; -- espera el bloqueo de Pedidos:p-1 → DEADLOCK
```

Ambas sesiones acceden a `Pedidos` y `Clientes`, pero **en orden inverso** — esa es la causa raíz estructural.

## Capturar el deadlock graph para diagnóstico

```sql
-- Habilitar captura de deadlocks en el Extended Events (o vía trace flag 1222 en el error log, método clásico)
CREATE EVENT SESSION CapturaDeadlocks ON SERVER
ADD EVENT sqlserver.xml_deadlock_report
ADD TARGET package0.event_file(SET filename = 'deadlocks.xel');

ALTER EVENT SESSION CapturaDeadlocks ON SERVER STATE = START;
```

El deadlock graph (XML) muestra exactamente qué recursos, qué queries y qué orden de bloqueo produjeron el ciclo — es la fuente de verdad para diagnosticar, no hay que adivinar a partir del error 1205 aislado.

## Prevención: orden consistente de acceso

La técnica más efectiva y estructural: **todas las transacciones que acceden a las mismas tablas deben hacerlo siempre en el mismo orden**.

```sql
-- Correcto: ambas sesiones acceden en el mismo orden (Pedidos, luego Clientes)
BEGIN TRAN;
UPDATE Pedidos SET Estado = 'CONFIRMADO' WHERE Id = @pedidoId;
UPDATE Clientes SET UltimaCompra = GETDATE() WHERE Id = @clienteId;
COMMIT;
```

Si estructuralmente todo el código sigue el mismo orden de acceso a tablas, un deadlock circular entre esas dos tablas es **imposible por diseño**, no solo improbable.

## Otras técnicas de mitigación

- **Transacciones cortas** (ver [`blocking.md`](./blocking.md)): menos tiempo manteniendo bloqueos, menos ventana de oportunidad para un ciclo.
- **`READ COMMITTED SNAPSHOT`**: reduce bloqueos de lectura, reduciendo también la superficie de deadlocks que involucran lectores.
- **Índices apropiados** (ver [`indexes.md`](./indexes.md)): una query sin índice adecuado escanea más filas de las necesarias, bloqueando más recursos de los estrictamente necesarios y aumentando la probabilidad de colisión con otra transacción.
- **Reintento automático en la aplicación**: dado que un deadlock es, por diseño de SQL Server, resuelto matando una transacción, la aplicación debe estar preparada para capturar el error 1205 y reintentar la transacción completa desde el principio (nunca reintentar solo la última sentencia) — ver [`../../standards/exceptions.md`](../../standards/exceptions.md), distinción entre errores transitorios y permanentes.

```java
// Java: reintento explícito ante deadlock (error 1205 de SQL Server)
@Retryable(retryFor = DeadlockLoserDataAccessException.class, maxAttempts = 3, backoff = @Backoff(delay = 100))
public void confirmarPedido(String id) {
    // la transacción completa se reintenta desde el principio, no solo la última sentencia
}
```

## Checklist

- [ ] ¿Se ha capturado el deadlock graph real antes de asumir la causa?
- [ ] ¿Todas las transacciones que acceden a las mismas tablas lo hacen en un orden consistente?
- [ ] ¿La aplicación reintenta automáticamente ante el error 1205, reintentando la transacción completa?
- [ ] ¿Se ha evaluado `READ COMMITTED SNAPSHOT` como mitigación estructural?

Ver también: [`blocking.md`](./blocking.md), [`indexes.md`](./indexes.md), [`../../standards/exceptions.md`](../../standards/exceptions.md).

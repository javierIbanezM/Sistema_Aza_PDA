# Recipe: Crear un Trigger (SQL Server)

Aplica: [`../knowledge/sqlserver/engine.md`](../knowledge/sqlserver/engine.md), [`../standards/naming.md`](../standards/naming.md), [`../standards/sql.md`](../standards/sql.md).

## Cuándo usar un trigger (y cuándo NO)

- **Usar**: auditoría automática a nivel de motor (independiente de qué aplicación escriba), mantenimiento de invariantes que deben cumplirse sin excepción incluso ante escrituras directas fuera de la aplicación (migraciones, scripts de soporte).
- **NO usar**: lógica de negocio que pertenece al dominio de la aplicación (ver [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md)) — un trigger oculta comportamiento en el motor de base de datos, invisible al leer el código de la aplicación, dificultando razonar sobre el sistema (ver [`../anti-patterns/spaghetti.md`](../anti-patterns/spaghetti.md) sobre flujo de control implícito).

## Pasos

### 1. Definir el propósito exacto (auditoría, no lógica de negocio)

```sql
-- Ejemplo: tabla de auditoría que registra cada cambio de estado de un pedido
CREATE TABLE PedidosAuditoria (
    Id BIGINT IDENTITY PRIMARY KEY,
    PedidoId VARCHAR(36) NOT NULL,
    EstadoAnterior VARCHAR(20) NULL,
    EstadoNuevo VARCHAR(20) NOT NULL,
    ModificadoPor SYSNAME NOT NULL DEFAULT SUSER_SNAME(),
    ModificadoEn DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
```

### 2. Crear el trigger, usando `AFTER` para no bloquear la escritura original innecesariamente

```sql
CREATE TRIGGER trg_Pedidos_AuditarCambioEstado
ON Pedidos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON; -- obligatorio, ver standards/sql.md

    -- Solo auditar si la columna Estado realmente cambió (no en cualquier UPDATE)
    IF UPDATE(Estado)
    BEGIN
        INSERT INTO PedidosAuditoria (PedidoId, EstadoAnterior, EstadoNuevo)
        SELECT
            i.Id,
            d.Estado,
            i.Estado
        FROM inserted i
        JOIN deleted d ON i.Id = d.Id
        WHERE i.Estado <> d.Estado;
    END
END;
```

`inserted`/`deleted` son las tablas virtuales que SQL Server provee dentro de un trigger — `deleted` contiene el estado anterior de las filas afectadas, `inserted` el estado nuevo (en `UPDATE`, ambas están pobladas; en `INSERT`, solo `inserted`; en `DELETE`, solo `deleted`).

### 3. Manejar múltiples filas (nunca asumir una única fila afectada)

El ejemplo anterior ya usa un `INSERT ... SELECT` sobre `inserted`/`deleted`, correcto para operaciones que afectan múltiples filas (`UPDATE` con `WHERE` que afecta a muchas filas a la vez) — **nunca** escribir un trigger que asume una única fila con `SELECT TOP 1` o variables escalares, ya que fallaría silenciosamente (procesando solo una fila) ante un `UPDATE` masivo.

### 4. Evitar lógica costosa dentro del trigger

Un trigger se ejecuta **dentro** de la misma transacción que la operación que lo dispara — cualquier lógica lenta dentro del trigger extiende la duración de esa transacción, aumentando el riesgo de blocking (ver [`../knowledge/sqlserver/blocking.md`](../knowledge/sqlserver/blocking.md)). Mantener los triggers simples y rápidos; lógica compleja/costosa debe vivir en la capa de aplicación o en un proceso asíncrono separado.

### 5. Test del trigger

```sql
-- Verificar que el trigger audita correctamente un cambio de estado
BEGIN TRAN;
    UPDATE Pedidos SET Estado = 'CONFIRMADO' WHERE Id = 'p-test-1';
    SELECT * FROM PedidosAuditoria WHERE PedidoId = 'p-test-1'; -- debe mostrar el registro nuevo
ROLLBACK; -- deshace el cambio de prueba, no afecta datos reales
```

## Checklist final

- [ ] ¿El trigger tiene `SET NOCOUNT ON`?
- [ ] ¿Maneja correctamente múltiples filas afectadas (usa `inserted`/`deleted` como conjuntos, no como fila única)?
- [ ] ¿Se ha verificado que la lógica dentro del trigger es rápida, sin I/O externo ni cálculos costosos?
- [ ] ¿El propósito es auditoría/invariante de datos, no lógica de negocio que debería vivir en la aplicación?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../knowledge/sqlserver/engine.md`](../knowledge/sqlserver/engine.md), [`../knowledge/sqlserver/security.md`](../knowledge/sqlserver/security.md) (auditoría como alternativa nativa de SQL Server Audit).

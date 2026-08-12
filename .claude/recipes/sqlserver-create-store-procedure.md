# Recipe: Crear un Stored Procedure (SQL Server)

Aplica: [`../standards/sql.md`](../standards/sql.md), [`../standards/naming.md`](../standards/naming.md), [`../knowledge/sqlserver/security.md`](../knowledge/sqlserver/security.md).

## Pasos

### 1. Definir la firma con parámetros tipados explícitamente

```sql
CREATE PROCEDURE dbo.sp_ConfirmarPedido
    @PedidoId VARCHAR(36),
    @UsuarioId VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON; -- obligatorio, ver standards/sql.md
    SET XACT_ABORT ON; -- ante error, revierte automáticamente la transacción completa
```

`SET XACT_ABORT ON` es especialmente importante: sin él, un error dentro de una transacción explícita no siempre provoca rollback automático, dejando la transacción en un estado ambiguo — con `XACT_ABORT ON`, cualquier error runtime aborta y revierte la transacción completa de forma predecible.

### 2. Validar precondiciones explícitamente, con mensajes de error claros

```sql
    IF NOT EXISTS (SELECT 1 FROM Pedidos WHERE Id = @PedidoId)
    BEGIN
        RAISERROR('Pedido %s no encontrado', 16, 1, @PedidoId);
        RETURN;
    END

    IF EXISTS (SELECT 1 FROM Pedidos WHERE Id = @PedidoId AND Estado = 'CONFIRMADO')
    BEGIN
        RAISERROR('El pedido %s ya está confirmado', 16, 1, @PedidoId);
        RETURN;
    END
```

Validar antes de modificar, con mensajes específicos — permite a la capa de aplicación (ver [`../standards/exceptions.md`](../standards/exceptions.md)) capturar el error y mapearlo a una excepción de dominio concreta, en lugar de un error genérico de SQL sin contexto.

### 3. Envolver en una transacción explícita si hay múltiples pasos

```sql
    BEGIN TRAN;

        UPDATE Pedidos SET Estado = 'CONFIRMADO', ConfirmadoEn = SYSUTCDATETIME()
        WHERE Id = @PedidoId;

        INSERT INTO PedidosAuditoria (PedidoId, EstadoNuevo, ModificadoPor)
        VALUES (@PedidoId, 'CONFIRMADO', @UsuarioId);

    COMMIT;
END;
```

Con `XACT_ABORT ON` ya activo, no es estrictamente necesario un `TRY...CATCH` para el rollback (ocurre automáticamente), pero si se necesita lógica de manejo de error personalizada (logging adicional, mensaje enriquecido), combinar ambos:

```sql
CREATE PROCEDURE dbo.sp_ConfirmarPedidoConManejoDeError
    @PedidoId VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

    BEGIN TRY
        BEGIN TRAN;
            UPDATE Pedidos SET Estado = 'CONFIRMADO' WHERE Id = @PedidoId;
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW; -- relanza el error original preservando el stack, ver standards/exceptions.md
    END CATCH
END;
```

`THROW` (sin argumentos, dentro de un `CATCH`) preserva el número de error, mensaje y severidad originales — preferible a `RAISERROR` al relanzar, que perdería esa información.

### 4. Permisos: otorgar solo `EXECUTE`, nunca acceso directo a las tablas subyacentes

```sql
GRANT EXECUTE ON dbo.sp_ConfirmarPedido TO RolAppPedidos;
```

Ver [`../knowledge/sqlserver/security.md`](../knowledge/sqlserver/security.md) — un patrón habitual de menor privilegio es que la aplicación solo tenga `EXECUTE` sobre procedimientos almacenados específicos, sin `SELECT`/`UPDATE` directo sobre las tablas, centralizando el control de acceso a nivel de procedimiento.

### 5. Invocación parametrizada desde la aplicación (nunca concatenada)

```java
// Java/JDBC — parametrizado, ver anti-patterns/sql-injection.md
CallableStatement stmt = connection.prepareCall("{call dbo.sp_ConfirmarPedido(?, ?)}");
stmt.setString(1, pedidoId);
stmt.setString(2, usuarioId);
stmt.execute();
```

### 6. Test del procedimiento

```sql
BEGIN TRAN;
    EXEC dbo.sp_ConfirmarPedido @PedidoId = 'p-test-1', @UsuarioId = 'u-test-1';
    SELECT Estado FROM Pedidos WHERE Id = 'p-test-1'; -- verificar 'CONFIRMADO'
ROLLBACK;
```

## Checklist final

- [ ] ¿El procedimiento tiene `SET NOCOUNT ON` y `SET XACT_ABORT ON`?
- [ ] ¿Las precondiciones se validan explícitamente con mensajes de error específicos?
- [ ] ¿Los errores se relanzan con `THROW` preservando el contexto original?
- [ ] ¿Los permisos otorgados son `EXECUTE` sobre el procedimiento, no acceso directo a las tablas?
- [ ] ¿La invocación desde la aplicación está parametrizada, nunca concatenada?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../standards/exceptions.md`](../standards/exceptions.md), [`../knowledge/sqlserver/security.md`](../knowledge/sqlserver/security.md).

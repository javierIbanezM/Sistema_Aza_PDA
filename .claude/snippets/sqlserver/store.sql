-- snippets/sqlserver/store.sql
-- Stored procedure con manejo de errores — ver recipes/sqlserver-create-store-procedure.md.

CREATE PROCEDURE dbo.sp_ConfirmarPedido
    @PedidoId VARCHAR(36),
    @UsuarioId VARCHAR(36)
AS
BEGIN
    SET NOCOUNT ON;
    SET XACT_ABORT ON;

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

    BEGIN TRY
        BEGIN TRAN;
            UPDATE Pedidos
            SET Estado = 'CONFIRMADO', ConfirmadoEn = SYSUTCDATETIME()
            WHERE Id = @PedidoId;

            INSERT INTO PedidosAuditoria (PedidoId, EstadoNuevo, ModificadoPor)
            VALUES (@PedidoId, 'CONFIRMADO', @UsuarioId);
        COMMIT;
    END TRY
    BEGIN CATCH
        IF @@TRANCOUNT > 0 ROLLBACK;
        THROW;
    END CATCH
END;
GO

GRANT EXECUTE ON dbo.sp_ConfirmarPedido TO RolAppPedidos;
GO

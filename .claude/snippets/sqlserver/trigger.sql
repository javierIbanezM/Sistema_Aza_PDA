-- snippets/sqlserver/trigger.sql
-- Trigger de auditoría — ver recipes/sqlserver-create-trigger.md para el proceso completo.

CREATE TABLE PedidosAuditoria (
    Id BIGINT IDENTITY PRIMARY KEY,
    PedidoId VARCHAR(36) NOT NULL,
    EstadoAnterior VARCHAR(20) NULL,
    EstadoNuevo VARCHAR(20) NOT NULL,
    ModificadoPor SYSNAME NOT NULL DEFAULT SUSER_SNAME(),
    ModificadoEn DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
);
GO

CREATE TRIGGER trg_Pedidos_AuditarCambioEstado
ON Pedidos
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;

    IF UPDATE(Estado)
    BEGIN
        INSERT INTO PedidosAuditoria (PedidoId, EstadoAnterior, EstadoNuevo)
        SELECT i.Id, d.Estado, i.Estado
        FROM inserted i
        JOIN deleted d ON i.Id = d.Id
        WHERE i.Estado <> d.Estado;
    END
END;
GO

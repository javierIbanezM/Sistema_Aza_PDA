-- snippets/sqlserver/merge.sql
-- Patrón MERGE (upsert) — usar con cautela: MERGE ha tenido bugs de concurrencia históricos en algunas
-- versiones de SQL Server bajo alta concurrencia; para upserts de alta concurrencia, considerar
-- alternativa con TRY_INSERT + UPDATE en su lugar (ver comentario al final).

MERGE INTO Inventario AS target
USING (SELECT @ProductoId AS ProductoId, @Cantidad AS Cantidad) AS source
    ON target.ProductoId = source.ProductoId
WHEN MATCHED THEN
    UPDATE SET target.Cantidad = target.Cantidad + source.Cantidad,
               target.ActualizadoEn = SYSUTCDATETIME()
WHEN NOT MATCHED THEN
    INSERT (ProductoId, Cantidad, ActualizadoEn)
    VALUES (source.ProductoId, source.Cantidad, SYSUTCDATETIME())
OUTPUT $action, inserted.ProductoId, inserted.Cantidad;

-- Alternativa más segura bajo alta concurrencia (evita el bug conocido de MERGE con race conditions):
--
-- BEGIN TRAN;
--     UPDATE Inventario WITH (UPDLOCK, HOLDLOCK)
--     SET Cantidad = Cantidad + @Cantidad, ActualizadoEn = SYSUTCDATETIME()
--     WHERE ProductoId = @ProductoId;
--
--     IF @@ROWCOUNT = 0
--         INSERT INTO Inventario (ProductoId, Cantidad, ActualizadoEn)
--         VALUES (@ProductoId, @Cantidad, SYSUTCDATETIME());
-- COMMIT;

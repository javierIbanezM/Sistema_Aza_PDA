# Recipe: Crear un Índice (SQL Server)

Aplica: [`../knowledge/sqlserver/indexes.md`](../knowledge/sqlserver/indexes.md), [`../knowledge/sqlserver/execution-plans.md`](../knowledge/sqlserver/execution-plans.md), [`../standards/naming.md`](../standards/naming.md).

## Pasos

### 1. Confirmar el problema con el plan de ejecución ANTES de crear el índice

```sql
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
GO
SELECT Id, Estado, Total FROM Pedidos WHERE ClienteId = @clienteId AND Estado = 'CONFIRMADO';
GO
```

Verificar en el plan (ver [`../knowledge/sqlserver/execution-plans.md`](../knowledge/sqlserver/execution-plans.md)) que efectivamente hay un `Scan` donde debería haber un `Seek` — nunca crear un índice especulativamente sin haber confirmado primero que resuelve un problema real medido.

### 2. Identificar las columnas de filtro, orden y las columnas adicionales seleccionadas

```sql
-- Query objetivo: WHERE ClienteId = ? AND Estado = ? ORDER BY FechaCreacion DESC
-- SELECT Id, Estado, Total, FechaCreacion
```

- Columnas de igualdad (`WHERE ... =`): `ClienteId`, `Estado`.
- Columnas de orden (`ORDER BY`): `FechaCreacion`.
- Columnas seleccionadas no usadas en filtro/orden: `Total` (candidata a `INCLUDE`).

### 3. Aplicar la regla ESR (Equality, Sort, Range) para el orden de columnas

```sql
CREATE NONCLUSTERED INDEX IX_Pedidos_ClienteId_Estado_FechaCreacion
ON Pedidos(ClienteId, Estado, FechaCreacion DESC)
INCLUDE (Total);
```

Ver [`../knowledge/sqlserver/indexes.md`](../knowledge/sqlserver/indexes.md) sección ESR — igualdad primero, luego ordenación, columnas de solo lectura en `INCLUDE` para convertirlo en *covering index* (evita Key Lookup).

### 4. Verificar la selectividad de las columnas de igualdad antes de confirmar

```sql
SELECT Estado, COUNT(*) AS Cantidad
FROM Pedidos
GROUP BY Estado
ORDER BY Cantidad DESC;
```

Si `Estado = 'CONFIRMADO'` representa más del ~30-40% de la tabla, el índice podría no aportar el beneficio esperado (el optimizador puede seguir prefiriendo un scan) — evaluar si `ClienteId` por sí solo ya es suficientemente selectivo.

### 5. Crear el índice con `ONLINE` en producción (si la edición lo soporta)

```sql
CREATE NONCLUSTERED INDEX IX_Pedidos_ClienteId_Estado_FechaCreacion
ON Pedidos(ClienteId, Estado, FechaCreacion DESC)
INCLUDE (Total)
WITH (ONLINE = ON, DATA_COMPRESSION = PAGE); -- ONLINE evita bloquear la tabla durante la creación (Enterprise)
```

`DATA_COMPRESSION = PAGE` reduce el tamaño en disco y en memoria (buffer pool) del índice a costa de algo de CPU — generalmente beneficioso en índices de lectura frecuente.

### 6. Verificar el nuevo plan y el impacto medido

```sql
SET STATISTICS IO ON;
GO
SELECT Id, Estado, Total FROM Pedidos WHERE ClienteId = @clienteId AND Estado = 'CONFIRMADO' ORDER BY FechaCreacion DESC;
GO
-- Confirmar: Index Seek en lugar de Scan, logical reads reducidos significativamente
```

### 7. Verificar que el índice se usa realmente tras un periodo en producción

```sql
SELECT s.user_seeks, s.user_scans, s.user_lookups, s.user_updates
FROM sys.dm_db_index_usage_stats s
JOIN sys.indexes i ON s.object_id = i.object_id AND s.index_id = i.index_id
WHERE i.name = 'IX_Pedidos_ClienteId_Estado_FechaCreacion';
```

Si tras varias semanas `user_seeks + user_scans + user_lookups` permanece en 0 mientras `user_updates` crece, el índice es puro coste sin beneficio — candidato a eliminar.

## Checklist final

- [ ] ¿Se confirmó el problema con un plan de ejecución real antes de crear el índice?
- [ ] ¿El orden de columnas sigue la regla ESR?
- [ ] ¿Se verificó la selectividad de las columnas de igualdad?
- [ ] ¿Se usó `INCLUDE` para las columnas seleccionadas no usadas en filtro/orden?
- [ ] ¿Se midió el plan/IO antes y después con la misma metodología?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../knowledge/sqlserver/indexes.md`](../knowledge/sqlserver/indexes.md), [`../knowledge/sqlserver/execution-plans.md`](../knowledge/sqlserver/execution-plans.md), [`sqlserver-optimize-query.md`](./sqlserver-optimize-query.md).

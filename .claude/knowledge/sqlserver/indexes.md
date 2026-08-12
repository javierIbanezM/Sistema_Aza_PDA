# Indexes

> Ver [`execution-plans.md`](./execution-plans.md) para cómo verificar si un índice se está usando realmente, y [`statistics.md`](./statistics.md) para los datos de los que depende el optimizador al decidir usarlo.

## Clustered vs. Non-Clustered

- **Clustered index**: define el **orden físico** de almacenamiento de las filas de la tabla. Solo puede existir uno por tabla (los datos solo pueden estar físicamente ordenados de una forma). Por defecto, se crea sobre la clave primaria.
- **Non-clustered index**: estructura separada que apunta a las filas de la tabla (vía la clave del clustered index, o vía RID si la tabla es un *heap* sin clustered index). Una tabla puede tener múltiples índices non-clustered.

```sql
-- Clustered index (normalmente ya creado implícitamente por la PK)
CREATE CLUSTERED INDEX IX_Pedidos_Id ON Pedidos(Id);

-- Non-clustered index sobre una columna de filtrado frecuente
CREATE NONCLUSTERED INDEX IX_Pedidos_ClienteId ON Pedidos(ClienteId);
```

## Columnas `INCLUDE`: evitar Key Lookups

```sql
CREATE NONCLUSTERED INDEX IX_Pedidos_ClienteId_Estado
ON Pedidos(ClienteId, Estado)
INCLUDE (Total, FechaCreacion);
```

Si una query selecciona `Total` y `FechaCreacion` además de filtrar por `ClienteId`/`Estado`, incluir esas columnas en el índice (`INCLUDE`) evita que SQL Server tenga que volver a la tabla base (`Key Lookup`, ver [`execution-plans.md`](./execution-plans.md)) para obtenerlas — el índice ya contiene todo lo necesario (*covering index*), la query se resuelve enteramente desde el índice.

## Orden de columnas en índices compuestos: regla ESR

**Equality, Sort, Range** — el orden de las columnas en un índice compuesto debe seguir:

1. Columnas de **igualdad** (`WHERE columna = valor`) primero.
2. Columnas de **ordenación** (`ORDER BY`) después.
3. Columnas de **rango** (`WHERE columna > valor`, `BETWEEN`) al final.

```sql
-- Query: WHERE Estado = 'CONFIRMADO' ORDER BY FechaCreacion DESC
-- donde FechaCreacion también se usa a veces con rango (WHERE FechaCreacion > @fecha)
CREATE NONCLUSTERED INDEX IX_Pedidos_Estado_Fecha
ON Pedidos(Estado, FechaCreacion DESC);
```

Un índice con las columnas en el orden incorrecto (rango antes que igualdad) no se puede aprovechar completamente — SQL Server puede usarlo parcialmente, pero no de la forma más eficiente posible.

## Selectividad: cuándo un índice NO ayuda

Un índice sobre una columna con baja cardinalidad (pocos valores distintos, ej. una columna `Activo BIT` con solo `0`/`1`) aporta poco beneficio — el optimizador probablemente prefiera un scan completo si más del ~5-10% de las filas coinciden con el filtro, ya que leer el índice y luego hacer lookups fila a fila puede ser más costoso que leer la tabla secuencialmente. Verificar la selectividad real de la columna antes de indexarla:

```sql
SELECT Estado, COUNT(*) AS Cantidad
FROM Pedidos
GROUP BY Estado
ORDER BY Cantidad DESC;
```

## Índices filtrados (Filtered Indexes)

```sql
CREATE NONCLUSTERED INDEX IX_Pedidos_Pendientes
ON Pedidos(FechaCreacion)
WHERE Estado = 'PENDIENTE';
```

Útil cuando las queries frecuentes siempre filtran por un subconjunto pequeño y estable de filas (pedidos pendientes, de un total con muchos estados posibles) — el índice es más pequeño, más rápido de mantener, y más selectivo que uno sobre la tabla completa.

## Mantenimiento: fragmentación

```sql
SELECT
    OBJECT_NAME(ips.object_id) AS Tabla,
    i.name AS Indice,
    ips.avg_fragmentation_in_percent
FROM sys.dm_db_index_physical_stats(DB_ID(), NULL, NULL, NULL, 'LIMITED') ips
JOIN sys.indexes i ON ips.object_id = i.object_id AND ips.index_id = i.index_id
WHERE ips.avg_fragmentation_in_percent > 10
ORDER BY ips.avg_fragmentation_in_percent DESC;
```

| Fragmentación | Acción recomendada |
|---|---|
| < 10% | Ninguna |
| 10-30% | `ALTER INDEX ... REORGANIZE` (online, menos intrusivo) |
| > 30% | `ALTER INDEX ... REBUILD` (más efectivo, puede bloquear salvo `ONLINE = ON` en Enterprise) |

Ver [`jobs.md`](./jobs.md) para automatizar este mantenimiento como job periódico.

## Coste de los índices: no indexar todo

Cada índice adicional acelera lecturas que lo usan, pero **ralentiza toda escritura** sobre la tabla (cada `INSERT`/`UPDATE`/`DELETE` debe mantener también cada índice afectado) y consume espacio en disco. Un índice que nunca se usa (verificable con `sys.dm_db_index_usage_stats`) es puro coste sin beneficio — candidato a eliminar.

```sql
SELECT
    OBJECT_NAME(s.object_id) AS Tabla,
    i.name AS Indice,
    s.user_seeks, s.user_scans, s.user_lookups, s.user_updates
FROM sys.dm_db_index_usage_stats s
JOIN sys.indexes i ON s.object_id = i.object_id AND s.index_id = i.index_id
WHERE s.user_updates > 0 AND (s.user_seeks + s.user_scans + s.user_lookups) = 0;
-- índices que se mantienen en cada escritura pero nunca se usan para lectura
```

## Checklist

- [ ] ¿Las columnas más consultadas por filtro/orden tienen un índice que las cubre según la regla ESR?
- [ ] ¿Se han añadido columnas `INCLUDE` para evitar Key Lookups en queries frecuentes?
- [ ] ¿Se ha verificado la selectividad real antes de indexar una columna de baja cardinalidad?
- [ ] ¿Existe un job de mantenimiento que reorganiza/reconstruye índices según su fragmentación?
- [ ] ¿Se han identificado y evaluado para eliminación los índices sin uso de lectura (`sys.dm_db_index_usage_stats`)?

Ver también: [`execution-plans.md`](./execution-plans.md), [`statistics.md`](./statistics.md), [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md).

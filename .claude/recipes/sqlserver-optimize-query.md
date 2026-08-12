# Recipe: Optimizar una Query Lenta (SQL Server)

Aplica: [`../prompts/performance.md`](../prompts/performance.md) (metodología general), [`../knowledge/sqlserver/execution-plans.md`](../knowledge/sqlserver/execution-plans.md), [`../knowledge/sqlserver/performance.md`](../knowledge/sqlserver/performance.md).

## Pasos

### 1. Medir antes de tocar nada

```sql
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
GO
-- ejecutar la query exactamente como la ejecuta la aplicación, con datos representativos
GO
```

Anotar: `logical reads`, `CPU time`, `elapsed time`. Esta es la línea base contra la que se comparará cualquier cambio.

### 2. Obtener el plan real y localizar el operador más costoso

```sql
SET STATISTICS XML ON;
GO
-- ejecutar la query
GO
```

Ver [`../knowledge/sqlserver/execution-plans.md`](../knowledge/sqlserver/execution-plans.md) — buscar `Clustered Index Scan`/`Index Scan` en tablas grandes, `Key Lookup` repetidos, warnings de conversión implícita, o discrepancia grande entre filas estimadas y reales.

### 3. Verificar sargabilidad de las condiciones `WHERE`

```sql
-- Non-SARGable (revisar si existe en la query real)
WHERE YEAR(FechaCreacion) = 2026
WHERE ISNULL(Estado, 'PENDIENTE') = 'PENDIENTE'

-- Reescribir como SARGable
WHERE FechaCreacion >= '2026-01-01' AND FechaCreacion < '2027-01-01'
```

Ver [`../knowledge/sqlserver/performance.md`](../knowledge/sqlserver/performance.md) sección SARGable — este paso por sí solo resuelve una proporción significativa de queries lentas sin necesidad de ningún índice nuevo.

### 4. Verificar conversiones implícitas de tipo

```sql
-- En el plan, buscar el warning de "Type Conversion" en el operador de filtro/join
-- Confirmar que el tipo del parámetro enviado desde la aplicación coincide EXACTAMENTE con la columna
```

Ver [`../knowledge/sqlserver/performance.md`](../knowledge/sqlserver/performance.md) — un desajuste `VARCHAR`/`NVARCHAR` entre columna y parámetro es una causa muy común y silenciosa.

### 5. Verificar estadísticas actualizadas

```sql
DBCC SHOW_STATISTICS('Pedidos', 'IX_Pedidos_ClienteId');
-- revisar "Updated" — si es antiguo y ha habido cambios significativos de volumen, actualizar:
UPDATE STATISTICS Pedidos WITH FULLSCAN;
```

Ver [`../knowledge/sqlserver/statistics.md`](../knowledge/sqlserver/statistics.md).

### 6. Si el plan indica ausencia de índice útil, crear uno (con la recipe correspondiente)

Ver [`sqlserver-create-index.md`](./sqlserver-create-index.md) — no crear un índice sin haber completado los pasos 3 y 4 primero, ya que a menudo resuelven el problema sin necesidad de índices adicionales.

### 7. Si la query involucra JOINs costosos, verificar el tipo de join elegido

```sql
-- Nested Loops: eficiente si la tabla externa es pequeña y hay índice en la interna
-- Hash Match: aceptable para joins grandes sin índice útil, pero costoso en memoria/tempdb
-- Verificar si un índice en la columna de JOIN cambiaría Hash Match por Nested Loops + Index Seek
```

Ver [`../knowledge/sqlserver/execution-plans.md`](../knowledge/sqlserver/execution-plans.md) tabla de operadores.

### 8. Volver a medir con la misma metodología del paso 1

```sql
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
GO
-- misma query, mismos datos
GO
```

Comparar `logical reads`/`elapsed time` antes/después — cuantificar la mejora real, no solo confirmar "parece más rápido".

## Checklist final

- [ ] ¿Se midió la línea base (`STATISTICS IO/TIME`) antes de cualquier cambio?
- [ ] ¿Se verificó sargabilidad y conversiones implícitas antes de crear cualquier índice nuevo?
- [ ] ¿Se verificaron estadísticas actualizadas?
- [ ] ¿Se comparó la medición final contra la línea base con la misma metodología?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`sqlserver-create-index.md`](./sqlserver-create-index.md), [`../knowledge/sqlserver/performance.md`](../knowledge/sqlserver/performance.md), [`../prompts/performance.md`](../prompts/performance.md).

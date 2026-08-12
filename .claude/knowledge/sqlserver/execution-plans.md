# Execution Plans

> Ver [`engine.md`](./engine.md) para el proceso de optimización que genera estos planes, y [`statistics.md`](./statistics.md) para los datos de los que depende el optimizador al elegirlos.

## Cómo obtener un plan de ejecución

```sql
-- Plan estimado (sin ejecutar la query realmente)
SET SHOWPLAN_XML ON;
GO
SELECT * FROM Pedidos WHERE ClienteId = 'c-123';
GO
SET SHOWPLAN_XML OFF;

-- Plan real (ejecuta la query y muestra estadísticas reales de filas procesadas)
SET STATISTICS XML ON;
GO
SELECT * FROM Pedidos WHERE ClienteId = 'c-123';
GO
SET STATISTICS XML OFF;
```

El **plan real** (`STATISTICS XML`/`STATISTICS PROFILE`) es preferible para diagnóstico — muestra el número de filas *realmente* procesadas en cada operador, permitiendo comparar contra la estimación del optimizador (una gran discrepancia entre estimado y real es la señal más común de estadísticas desactualizadas, ver [`statistics.md`](./statistics.md)).

## Operadores más comunes y su coste relativo

| Operador | Significado | Coste típico |
|---|---|---|
| `Clustered Index Scan` | Recorre toda la tabla (vía su índice clustered) | Alto en tablas grandes — señal de ausencia de índice útil |
| `Clustered Index Seek` | Busca directamente las filas necesarias usando el índice clustered | Bajo — el caso ideal |
| `Index Scan` (non-clustered) | Recorre todo un índice no clustered | Alto, pero menor que un scan de tabla completa |
| `Index Seek` (non-clustered) | Busca directamente en un índice no clustered | Bajo — el caso ideal para queries selectivas |
| `Key Lookup` / `RID Lookup` | Tras un Index Seek, busca columnas adicionales no incluidas en el índice, fila por fila | Puede ser costoso si ocurre muchas veces — ver [`indexes.md`](./indexes.md), columnas `INCLUDE` |
| `Hash Match` | Join mediante tabla hash en memoria/tempdb | Aceptable para joins grandes sin índice útil, pero costoso en memoria/tempdb |
| `Nested Loops` | Join iterando la tabla externa y buscando en la interna por cada fila | Eficiente si la tabla externa es pequeña y hay índice en la interna |
| `Sort` | Ordena el resultado intermedio | Costoso en datasets grandes — evaluar si un índice ya ordenado evitaría este paso |

## Leer el coste porcentual (con cautela)

Cada operador muestra un "% coste relativo del batch" — útil para identificar rápidamente el operador más costoso de la query, pero es una **estimación basada en heurísticas del optimizador**, no una medición real de tiempo. Combinar siempre con `SET STATISTICS TIME ON`/`SET STATISTICS IO ON` para tiempos y I/O reales.

```sql
SET STATISTICS IO ON;
SET STATISTICS TIME ON;
GO
SELECT * FROM Pedidos WHERE ClienteId = 'c-123';
GO
```

`STATISTICS IO` muestra `logical reads` (páginas leídas de caché) y `physical reads` (páginas leídas de disco) por tabla — un número alto de `logical reads` para una query que debería ser selectiva es la señal más directa de un índice ausente o mal diseñado.

## Warnings a vigilar en el plan

- **"Missing Index"**: SQL Server sugiere un índice que reduciría el coste — evaluar la sugerencia críticamente (ver [`indexes.md`](./indexes.md)), no crear ciegamente todo índice sugerido, ya que cada índice tiene coste de escritura.
- **Icono de warning amarillo en un operador**: suele indicar conversión implícita de tipos (comparar una columna `VARCHAR` con un parámetro `NVARCHAR`, por ejemplo), que impide usar un índice existente de forma eficiente.
- **Grosor de las flechas entre operadores**: representa el número estimado de filas que fluyen entre operadores — una flecha mucho más gruesa de lo esperado en un punto intermedio señala una estimación de cardinalidad incorrecta.

## Plan cacheado y reutilización (Plan Cache)

SQL Server cachea planes de ejecución para reutilizarlos ante ejecuciones futuras de la misma query (parametrizada). Implicaciones prácticas:

- Queries con literales embebidos en lugar de parámetros (`WHERE ClienteId = 'c-123'` repetido con valores distintos cada vez) generan un plan nuevo por cada valor distinto, saturando la caché de planes sin reutilización — usar parámetros (`sp_executesql`, procedimientos almacenados) en su lugar.
- **Parameter sniffing**: el primer plan generado para un procedimiento almacenado se basa en los parámetros de esa primera ejecución; si la distribución de datos es muy asimétrica (un cliente con 10 pedidos vs. uno con 10 millones), el plan óptimo para uno puede ser pésimo para el otro. Mitigación: `OPTION (RECOMPILE)` en la query específica afectada, o `OPTIMIZE FOR UNKNOWN` si se prefiere un plan basado en el promedio estadístico general en lugar del valor específico del primer parámetro.

## Checklist

- [ ] ¿Se ha revisado el plan real (no solo el estimado) con `STATISTICS XML`/`IO`/`TIME` antes de optimizar?
- [ ] ¿Existen `Clustered Index Scan`/`Index Scan` en tablas grandes que podrían resolverse con un `Seek`?
- [ ] ¿Hay `Key Lookup` repetidos que un índice `INCLUDE` eliminaría?
- [ ] ¿Se ha descartado parameter sniffing como causa de una query lenta de forma intermitente?

Ver también: [`indexes.md`](./indexes.md), [`statistics.md`](./statistics.md), [`performance.md`](./performance.md).

# Statistics

> Ver [`execution-plans.md`](./execution-plans.md) — las estadísticas son el insumo principal del que depende el optimizador para elegir un plan de ejecución. Estadísticas desactualizadas son la causa más común de planes subóptimos que "antes funcionaban bien".

## Qué son y para qué sirven

Las estadísticas describen la **distribución de valores** de una columna (o conjunto de columnas) mediante un histograma — cuántos valores distintos existen, cómo se distribuyen, cuáles son más frecuentes. El optimizador las usa para estimar cuántas filas devolverá un filtro (`WHERE columna = valor`) **antes** de ejecutar la query, y esa estimación determina qué plan elige (¿un Seek selectivo, o directamente un Scan porque se espera que la mayoría de filas coincidan?).

## Ver las estadísticas de una columna/índice

```sql
DBCC SHOW_STATISTICS('Pedidos', 'IX_Pedidos_ClienteId');
```

Devuelve tres secciones: metadatos generales (fecha de última actualización, filas muestreadas), densidad (cuántas filas distintas por valor en promedio), e histograma (rangos de valores y su frecuencia estimada).

## Actualización automática vs. manual

Por defecto, SQL Server actualiza estadísticas automáticamente cuando el porcentaje de filas modificadas desde la última actualización supera un umbral (~20% históricamente, con umbrales dinámicos más agresivos en tablas grandes desde SQL Server 2016 con el trace flag/nivel de compatibilidad adecuado). Esto es **asíncrono por defecto** en muchas configuraciones — la query que dispara la actualización puede ejecutarse con las estadísticas aún desactualizadas mientras la actualización ocurre en segundo plano.

```sql
-- Forzar actualización manual, útil tras una carga masiva de datos
UPDATE STATISTICS Pedidos WITH FULLSCAN;

-- Actualización de todas las estadísticas de la base de datos
EXEC sp_updatestats;
```

`WITH FULLSCAN` recalcula sobre el 100% de los datos (más preciso, más costoso); sin especificarlo, SQL Server usa un muestreo (más rápido, potencialmente menos preciso en distribuciones muy asimétricas).

## Cuándo actualizar manualmente

- **Tras una carga masiva de datos** (`BULK INSERT`, migración, importación grande) — no esperar a que el umbral automático se dispare, ya que el primer query tras la carga puede sufrir un plan pésimo basado en estadísticas pre-carga.
- **Tras un cambio de distribución de datos significativo** sin cambio proporcional en el volumen total de filas (ej. una columna que pasó de tener 3 valores distintos a tener miles).
- Como parte de un job de mantenimiento periódico (ver [`jobs.md`](./jobs.md)), combinado con el mantenimiento de índices.

## Estadísticas y parameter sniffing

Ver [`execution-plans.md`](./execution-plans.md) para el detalle completo de parameter sniffing — las estadísticas son la causa raíz: el optimizador estima la cardinalidad esperada basándose en el histograma y el valor del parámetro de la **primera** ejecución, generando un plan que puede ser inapropiado para ejecuciones posteriores con parámetros de distribución muy distinta.

## Estadísticas multi-columna

```sql
CREATE STATISTICS Stats_Pedidos_Estado_ClienteId
ON Pedidos(Estado, ClienteId);
```

Cuando dos columnas se filtran juntas frecuentemente y su combinación tiene una correlación no capturada por las estadísticas individuales de cada columna por separado, unas estadísticas multi-columna (o un índice compuesto, que las genera automáticamente) mejoran la estimación de cardinalidad del optimizador para esa combinación específica.

## Checklist

- [ ] ¿Se han actualizado estadísticas manualmente tras cualquier carga masiva de datos?
- [ ] ¿Existe un job periódico de actualización de estadísticas, no solo dependencia del umbral automático?
- [ ] ¿Se ha verificado la fecha de última actualización de estadísticas (`DBCC SHOW_STATISTICS`) al diagnosticar una query lenta con estimaciones de cardinalidad incorrectas?

Ver también: [`execution-plans.md`](./execution-plans.md), [`indexes.md`](./indexes.md), [`jobs.md`](./jobs.md).

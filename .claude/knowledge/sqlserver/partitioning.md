# Partitioning

> Ver [`indexes.md`](./indexes.md) — las tablas particionadas requieren índices alineados con el esquema de particiones para obtener el beneficio completo.

## Qué resuelve el particionamiento

Divide una tabla (lógicamente una única tabla desde el punto de vista de las queries) en múltiples unidades de almacenamiento físico separadas según el valor de una columna (típicamente una fecha). Beneficios principales: **partition elimination** (queries que filtran por la columna de partición solo escanean las particiones relevantes, no la tabla completa) y **mantenimiento por partición** (reconstruir un índice o purgar datos antiguos afecta solo a una partición, no a toda la tabla).

## Cuándo particionar (y cuándo NO)

- **Sí**: tablas muy grandes (decenas/cientos de millones de filas) con un patrón de acceso claramente segmentable por una columna (histórico por fecha, por región, por tenant), especialmente si hay una necesidad recurrente de purgar/archivar datos antiguos completos.
- **No**: tablas de tamaño moderado donde un buen índice (ver [`indexes.md`](./indexes.md)) ya resuelve el rendimiento de las queries — particionar añade complejidad operativa (mantenimiento de función/esquema de partición, planificación de nuevas particiones) que solo se justifica con el volumen y el patrón de purga que realmente la necesitan (ver YAGNI en [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)).

## Función y esquema de partición

```sql
-- 1. Función de partición: define los límites (una partición por año)
CREATE PARTITION FUNCTION PF_PedidosPorAnio (DATETIME)
AS RANGE RIGHT FOR VALUES ('2024-01-01', '2025-01-01', '2026-01-01');

-- 2. Esquema de partición: mapea cada partición a un filegroup físico
CREATE PARTITION SCHEME PS_PedidosPorAnio
AS PARTITION PF_PedidosPorAnio
ALL TO ([PRIMARY]); -- o filegroups específicos por partición para distribuir en distintos discos

-- 3. Tabla particionada
CREATE TABLE Pedidos (
    Id VARCHAR(36) NOT NULL,
    FechaCreacion DATETIME NOT NULL,
    ClienteId VARCHAR(36) NOT NULL,
    Estado VARCHAR(20) NOT NULL,
    CONSTRAINT PK_Pedidos PRIMARY KEY (Id, FechaCreacion) -- la columna de partición debe formar parte de la PK
) ON PS_PedidosPorAnio(FechaCreacion);
```

La columna de partición debe estar incluida en cualquier clave única/primaria de la tabla — restricción del motor, no una opción de diseño.

## Partition elimination: verificar que ocurre realmente

```sql
SELECT * FROM Pedidos WHERE FechaCreacion >= '2026-01-01' AND FechaCreacion < '2026-02-01';
```

En el plan de ejecución (ver [`execution-plans.md`](./execution-plans.md)), verificar la propiedad `Actual Partition Count` del operador de scan/seek — debe mostrar solo el número de particiones realmente relevantes para el filtro (idealmente 1), no todas las particiones de la tabla. Si la query no filtra directamente por la columna de partición (o lo hace de forma no sargable, ver [`performance.md`](./performance.md)), partition elimination no ocurre y se pierde el beneficio principal.

## Cambio de partición (Partition Switching): purga/archivado casi instantáneo

```sql
-- Mover todos los datos de la partición más antigua a una tabla de archivo, sin copiar filas físicamente
ALTER TABLE Pedidos SWITCH PARTITION 1 TO PedidosArchivo PARTITION 1;
```

`SWITCH` es una operación de metadatos (cambia a qué tabla "pertenece" la partición), no copia ni mueve datos físicamente — purgar un año completo de datos históricos pasa de ser una operación de `DELETE` masivo (costosa, genera mucho log de transacciones, bloquea) a una operación casi instantánea. Requiere que el esquema de la tabla origen y destino coincida exactamente, incluidos los índices.

## Mantenimiento de índices por partición

```sql
ALTER INDEX IX_Pedidos_ClienteId ON Pedidos REBUILD PARTITION = 3;
```

Reconstruir/reorganizar solo la partición con fragmentación relevante (típicamente la más reciente, con más actividad de escritura), en lugar de la tabla completa — reduce drásticamente el tiempo y el impacto del mantenimiento en tablas grandes. Ver [`indexes.md`](./indexes.md) para el criterio de fragmentación, y [`jobs.md`](./jobs.md) para automatizarlo.

## Checklist

- [ ] ¿El volumen y patrón de purga de la tabla justifican la complejidad operativa del particionamiento (frente a un buen índice simple)?
- [ ] ¿Las queries frecuentes filtran directamente por la columna de partición, de forma sargable, para beneficiarse de partition elimination?
- [ ] ¿Se usa `SWITCH PARTITION` para purga/archivado en lugar de `DELETE` masivo?
- [ ] ¿El mantenimiento de índices se ejecuta por partición, no sobre la tabla completa innecesariamente?

Ver también: [`indexes.md`](./indexes.md), [`execution-plans.md`](./execution-plans.md), [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md).

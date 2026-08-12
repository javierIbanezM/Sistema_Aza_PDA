# TempDB

> Ver [`blocking.md`](./blocking.md) — `READ COMMITTED SNAPSHOT` depende de TempDB para almacenar versiones de filas, uno de los motivos por los que su dimensionamiento correcto es crítico.

## Qué se almacena en TempDB

- **Objetos temporales explícitos**: tablas temporales (`#tabla`), variables de tabla (`@tabla`).
- **Row versioning**: versiones anteriores de filas para `READ COMMITTED SNAPSHOT`/`SNAPSHOT` isolation (ver [`blocking.md`](./blocking.md)).
- **Resultados intermedios de queries**: operaciones de `Sort`, `Hash Match` (ver [`execution-plans.md`](./execution-plans.md)) que no caben en memoria se derraman ("spill") a TempDB.
- **Índices con `SORT_IN_TEMPDB`**: reconstrucción de índices configurada para usar TempDB como espacio de trabajo en lugar de la base de datos de origen.
- **Cursores y triggers**: estructuras internas de soporte.

TempDB es **compartida por toda la instancia** — un uso excesivo de TempDB por una base de datos/aplicación afecta a todas las demás bases de datos de la misma instancia.

## Múltiples archivos de datos: reducir contención en páginas de asignación

```sql
-- Configuración recomendada: un archivo de datos por núcleo lógico, hasta 8
ALTER DATABASE tempdb ADD FILE (
    NAME = tempdev2,
    FILENAME = 'D:\TempDB\tempdb2.ndf',
    SIZE = 1024MB,
    FILEGROWTH = 256MB
);
-- Repetir hasta tener 4-8 archivos de datos, todos del MISMO tamaño inicial y mismo crecimiento
```

Regla práctica: **hasta 8 núcleos lógicos → un archivo de datos por núcleo; más de 8 núcleos → empezar con 8 y añadir de 4 en 4 solo si se observa contención residual** (verificable con la espera `PAGELATCH` sobre páginas `PFS`/`GAM`/`SGAM` en `sys.dm_os_wait_stats`). Múltiples archivos permiten que SQL Server reparta la asignación de espacio entre ellos en round-robin, reduciendo la contención en las páginas de asignación internas que un único archivo concentraría.

**Todos los archivos deben tener el mismo tamaño inicial y el mismo crecimiento** — de lo contrario, el algoritmo de asignación proporcional favorece al archivo con más espacio libre, anulando el beneficio de repartir la carga.

## Dimensionamiento: pre-asignar, evitar auto-growth frecuente

```sql
ALTER DATABASE tempdb MODIFY FILE (NAME = tempdev, SIZE = 8192MB, FILEGROWTH = 1024MB);
```

Cada evento de auto-growth es una operación costosa que pausa las operaciones en curso. Dimensionar TempDB desde el inicio con un tamaño que cubra la carga de trabajo esperada (basado en el pico de uso observado), en lugar de dejar que crezca reactivamente evento a evento.

## Diagnóstico de contención en TempDB

```sql
SELECT
    session_id, request_id,
    SUM(user_objects_alloc_page_count) AS PaginasObjetosTemp,
    SUM(internal_objects_alloc_page_count) AS PaginasInternas
FROM sys.dm_db_task_space_usage
GROUP BY session_id, request_id
ORDER BY PaginasInternas DESC;
```

Identifica qué sesiones están consumiendo más espacio de TempDB en un momento dado — útil para localizar la query responsable de un pico de uso o de contención.

## Monitorización de espacio libre

```sql
SELECT
    SUM(unallocated_extent_page_count) * 8 / 1024 AS EspacioLibreMB
FROM sys.dm_db_file_space_usage;
```

TempDB llena al 100% detiene operaciones en toda la instancia (no solo en la base de datos que lo causó) — requiere alertas de monitorización propias, con margen de reacción antes de llegar al límite.

## Checklist

- [ ] ¿TempDB tiene múltiples archivos de datos del mismo tamaño, según el número de núcleos lógicos?
- [ ] ¿El tamaño inicial de TempDB está dimensionado para la carga esperada, evitando auto-growth reactivo frecuente?
- [ ] ¿Existe monitorización de espacio libre y de contención (`PAGELATCH` en páginas de asignación)?
- [ ] ¿Se ha evaluado el impacto de `READ COMMITTED SNAPSHOT` (ver [`blocking.md`](./blocking.md)) sobre el uso de TempDB antes de habilitarlo en una base de datos de alto volumen?

Ver también: [`blocking.md`](./blocking.md), [`execution-plans.md`](./execution-plans.md), [`performance.md`](./performance.md).

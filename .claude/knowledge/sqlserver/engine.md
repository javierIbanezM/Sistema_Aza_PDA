# SQL Server Engine

> Fundamentos de la arquitectura interna de SQL Server necesarios para entender el resto de documentos de este dominio (índices, planes de ejecución, bloqueos). Ver [`../architecture/clean-architecture.md`](../architecture/clean-architecture.md) para cómo este conocimiento se integra en la capa de infraestructura de una aplicación.

## Arquitectura de almacenamiento

- **Páginas**: unidad fija de almacenamiento en disco, 8 KB. Toda fila de una tabla vive dentro de una página; una tabla ocupa un número entero de páginas.
- **Extents**: agrupación de 8 páginas contiguas (64 KB). SQL Server asigna espacio en extents, no en páginas individuales, para reducir fragmentación.
- **Filegroups**: agrupación lógica de archivos de datos (`.mdf`/`.ndf`). Permiten distribuir tablas/índices entre distintos discos físicos por rendimiento, o separar datos históricos poco accedidos de datos activos.

## Motor de ejecución de queries

1. **Parsing**: verifica la sintaxis de la query.
2. **Binding (algebrizing)**: resuelve nombres de objetos (tablas, columnas) contra el catálogo del sistema.
3. **Optimización**: el Query Optimizer genera múltiples planes de ejecución candidatos y elige el de menor coste estimado, basándose en estadísticas (ver [`statistics.md`](./statistics.md)) — no garantiza el plan óptimo absoluto, sino uno "suficientemente bueno" dentro de un presupuesto de tiempo de optimización.
4. **Ejecución**: el plan elegido se ejecuta, produciendo el resultado.

Ver [`execution-plans.md`](./execution-plans.md) para leer e interpretar el resultado de este proceso.

## Modelos de recuperación (Recovery Models)

| Modelo | Comportamiento del log de transacciones | Uso típico |
|---|---|---|
| `SIMPLE` | El log se trunca automáticamente tras cada checkpoint | Entornos de desarrollo/test, datos recreables |
| `FULL` | El log se conserva hasta un backup de log explícito, permite restauración point-in-time | Producción, cuando la pérdida de datos debe minimizarse al máximo |
| `BULK_LOGGED` | Como `FULL`, pero minimiza el log de operaciones masivas (`BULK INSERT`) | Cargas masivas puntuales, con ventana de riesgo reducida durante la operación |

Ver [`backup.md`](./backup.md) y [`restore.md`](./restore.md) para la estrategia de backup asociada a cada modelo.

## Niveles de aislamiento de transacciones

| Nivel | Lecturas sucias | Lecturas no repetibles | Phantom reads | Mecanismo |
|---|---|---|---|---|
| `READ UNCOMMITTED` | Sí | Sí | Sí | Sin bloqueos de lectura |
| `READ COMMITTED` (por defecto) | No | Sí | Sí | Bloqueo de lectura liberado tras cada sentencia |
| `REPEATABLE READ` | No | No | Sí | Bloqueos de lectura mantenidos hasta el fin de la transacción |
| `SERIALIZABLE` | No | No | No | Bloqueos de rango, máxima consistencia, máxima contención |
| `READ COMMITTED SNAPSHOT` | No | Sí | Sí | Versionado de filas (row versioning), sin bloqueos de lectura — ver [`blocking.md`](./blocking.md) |

`READ COMMITTED SNAPSHOT` es la opción recomendada por defecto en sistemas OLTP de alta concurrencia (ver [`blocking.md`](./blocking.md)) — reduce drásticamente el bloqueo entre lectores y escritores sin sacrificar la consistencia de `READ COMMITTED`.

## Instancia, bases de datos del sistema

- `master`: metadatos de toda la instancia (logins, configuración, referencias a otras bases de datos).
- `msdb`: metadatos de SQL Server Agent (jobs, ver [`jobs.md`](./jobs.md)), backups, históricos.
- `model`: plantilla usada al crear una base de datos nueva.
- `tempdb`: espacio de trabajo temporal compartido por toda la instancia — ver [`tempdb.md`](./tempdb.md), fuente frecuente de contención si no se dimensiona correctamente.

## Checklist

- [ ] ¿Se ha elegido el modelo de recuperación (`SIMPLE`/`FULL`/`BULK_LOGGED`) según los requisitos reales de recuperación ante desastres del sistema?
- [ ] ¿Se ha evaluado `READ COMMITTED SNAPSHOT` para sistemas OLTP con contención de lecturas/escrituras?
- [ ] ¿Los filegroups separan datos de alta actividad de datos históricos/poco accedidos cuando el volumen lo justifica?

Ver también: [`execution-plans.md`](./execution-plans.md), [`blocking.md`](./blocking.md), [`tempdb.md`](./tempdb.md).

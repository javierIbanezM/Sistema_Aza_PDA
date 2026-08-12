# Restore

> Ver [`backup.md`](./backup.md) para los tipos de backup que este documento combina en el proceso de restauración.

## Restauración point-in-time (Full + Differential + Log)

```sql
-- 1. Restaurar el Full más reciente antes del punto deseado, SIN recuperar aún (NORECOVERY)
RESTORE DATABASE PedidosDB
FROM DISK = 'D:\Backups\PedidosDB_Full.bak'
WITH NORECOVERY, REPLACE;

-- 2. Restaurar el Differential más reciente antes del punto deseado (si existe), SIN recuperar aún
RESTORE DATABASE PedidosDB
FROM DISK = 'D:\Backups\PedidosDB_Diff.bak'
WITH NORECOVERY;

-- 3. Restaurar los backups de Log en orden, hasta el punto exacto deseado
RESTORE LOG PedidosDB
FROM DISK = 'D:\Backups\PedidosDB_Log_1.trn'
WITH NORECOVERY;

RESTORE LOG PedidosDB
FROM DISK = 'D:\Backups\PedidosDB_Log_2.trn'
WITH NORECOVERY, STOPAT = '2026-07-30 14:32:00'; -- el instante exacto deseado, dentro de este log

-- 4. Recuperar la base de datos (la deja online y consultable)
RESTORE DATABASE PedidosDB WITH RECOVERY;
```

`NORECOVERY` mantiene la base de datos en estado "restaurando", permitiendo aplicar el siguiente backup de la cadena; `RECOVERY` (solo en el último paso) confirma la restauración y deja la base de datos operativa. Aplicar `RECOVERY` prematuramente en un paso intermedio impide continuar aplicando más backups de log después.

## Verificar la cadena de backups antes de restaurar

```sql
RESTORE HEADERONLY FROM DISK = 'D:\Backups\PedidosDB_Log_2.trn';
-- Muestra FirstLSN/LastLSN — verificar que no hay huecos entre el LastLSN de un log
-- y el FirstLSN del siguiente antes de confiar en la cadena completa
```

Un backup de log faltante en la secuencia rompe la cadena — no se puede "saltar" un log intermedio y continuar con el siguiente; la restauración point-in-time solo es posible hasta el último punto cubierto por una cadena ininterrumpida.

## Restauración a un servidor distinto (para simulacros o entornos de test)

```sql
RESTORE DATABASE PedidosDB_Test
FROM DISK = 'D:\Backups\PedidosDB_Full.bak'
WITH MOVE 'PedidosDB' TO 'E:\Data\PedidosDB_Test.mdf',
     MOVE 'PedidosDB_log' TO 'E:\Data\PedidosDB_Test_log.ldf',
     RECOVERY;
```

`WITH MOVE` es necesario al restaurar con un nombre distinto o en una instancia distinta con rutas de archivo diferentes — sin esto, SQL Server intenta escribir en las rutas originales del servidor de origen.

## Simulacro de restauración: la única verificación real

Ver [`backup.md`](./backup.md) — `RESTORE VERIFYONLY` solo confirma que el archivo es legible, no que el proceso completo de restauración (incluida la cadena de log completa hasta el RPO objetivo) funciona en la práctica. Un simulacro periódico debe:

1. Restaurar la cadena completa (Full + Differential + Logs) en un entorno separado.
2. Verificar que la base de datos resultante es consultable y los datos son consistentes.
3. Medir el tiempo total del proceso — este tiempo medido es el **Recovery Time Objective (RTO) real**, no el teórico asumido sin haberlo probado nunca.

## Recovery Point Objective (RPO) y Recovery Time Objective (RTO)

| Métrica | Qué mide | Determinado por |
|---|---|---|
| RPO | Cuánto dato como máximo se puede permitir perder | Frecuencia de backup de log (ver [`backup.md`](./backup.md)) |
| RTO | Cuánto tiempo como máximo puede tardar la recuperación completa | Tamaño de la base de datos, velocidad de I/O del almacenamiento de backup, tiempo medido en simulacros reales |

Ambos deben ser requisitos de negocio explícitos, acordados antes de diseñar la estrategia de backup/restore — no valores que se descubren únicamente durante un incidente real.

## Checklist

- [ ] ¿Se ha verificado que la cadena de backups (Full → Differential → Logs) está completa sin huecos antes de una restauración crítica?
- [ ] ¿El RTO objetivo se ha medido en un simulacro real, no solo estimado teóricamente?
- [ ] ¿Existe un procedimiento documentado y probado de restauración a un servidor/instancia distinto para entornos de test?
- [ ] ¿El equipo sabe ejecutar una restauración point-in-time sin necesidad de investigar la sintaxis durante un incidente real?

Ver también: [`backup.md`](./backup.md), [`engine.md`](./engine.md).

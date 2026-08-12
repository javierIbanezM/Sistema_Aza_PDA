# Backup

> Ver [`engine.md`](./engine.md) para los modelos de recuperación (`SIMPLE`/`FULL`/`BULK_LOGGED`) que determinan qué estrategias de backup son posibles, y [`restore.md`](./restore.md) para el proceso complementario de restauración.

## Tipos de backup

| Tipo | Contenido | Requiere modelo |
|---|---|---|
| **Full** | Copia completa de la base de datos en el momento de ejecución | Cualquiera |
| **Differential** | Cambios desde el último Full | Cualquiera |
| **Log** | Transacciones desde el último backup de log (o desde el Full si es el primero) | `FULL` o `BULK_LOGGED` únicamente — `SIMPLE` trunca el log automáticamente, no permite backup de log |

## Estrategia estándar: Full + Differential + Log

```sql
-- Full semanal
BACKUP DATABASE PedidosDB
TO DISK = 'D:\Backups\PedidosDB_Full.bak'
WITH COMPRESSION, CHECKSUM, INIT;

-- Differential diario
BACKUP DATABASE PedidosDB
TO DISK = 'D:\Backups\PedidosDB_Diff.bak'
WITH DIFFERENTIAL, COMPRESSION, CHECKSUM;

-- Log cada 15 minutos (en modelo FULL)
BACKUP LOG PedidosDB
TO DISK = 'D:\Backups\PedidosDB_Log.trn'
WITH COMPRESSION, CHECKSUM;
```

La frecuencia de backup de log determina el **Recovery Point Objective (RPO)** real del sistema — con backups de log cada 15 minutos, la pérdida de datos máxima ante un desastre es de 15 minutos (más el tiempo desde el último log hasta el fallo). Ver [`restore.md`](./restore.md) para cómo se combinan estos tres tipos en una restauración point-in-time.

## `WITH CHECKSUM`: verificar integridad, siempre

`CHECKSUM` calcula y almacena un checksum de las páginas durante el backup, permitiendo detectar corrupción de datos en el momento del backup (mejor detectarla ahí que descubrirla al intentar restaurar meses después) — habilitar siempre, el coste de CPU adicional es mínimo comparado con el riesgo de un backup silenciosamente corrupto.

## `WITH COMPRESSION`: reduce tamaño y tiempo de I/O

La compresión de backup reduce tanto el espacio en disco como, contraintuitivamente, el tiempo total del backup en la mayoría de casos (menos bytes que escribir a disco compensa el coste de CPU de comprimir) — habilitar por defecto salvo limitación específica de CPU disponible durante la ventana de backup.

## Verificación de que el backup es restaurable

```sql
RESTORE VERIFYONLY FROM DISK = 'D:\Backups\PedidosDB_Full.bak';
```

Verifica que el archivo de backup está completo y legible — **no** verifica que los datos dentro sean lógicamente correctos ni que el proceso de restauración completo funcione. La única verificación realmente confiable es un **simulacro de restauración periódico** en un entorno separado (ver [`restore.md`](./restore.md)) — un backup nunca probado no es una estrategia de recuperación, es una suposición.

## Backup a almacenamiento redundante/externo

Un backup almacenado en el mismo servidor/disco que la base de datos original no protege contra fallos de hardware, ni contra ransomware que cifra todo el volumen. Estrategia mínima: backups replicados a almacenamiento externo (otro servidor, storage de red, cloud), con al menos una copia offline/inmutable no accesible desde la red de producción — mitigación específica contra ransomware que busca y cifra también los backups accesibles.

## Retención

```sql
-- Ejemplo de política de retención aplicada vía script de limpieza tras copiar a almacenamiento externo
-- Full: 4 semanas, Differential: 7 días, Log: 48 horas (ajustar según RPO/RTO del negocio)
```

La retención debe alinearse con requisitos de negocio (cuánto tiempo atrás se necesita poder restaurar) y regulatorios (algunos sectores exigen retención mínima de backups por normativa) — no un valor arbitrario.

## Checklist

- [ ] ¿El modelo de recuperación (`FULL`/`SIMPLE`) es coherente con el RPO requerido por el negocio?
- [ ] ¿Todos los backups usan `WITH CHECKSUM` y `WITH COMPRESSION`?
- [ ] ¿Existe una copia de los backups en almacenamiento externo/offline, no solo en el mismo servidor?
- [ ] ¿Se han realizado simulacros de restauración periódicos, no solo `RESTORE VERIFYONLY`?
- [ ] ¿La política de retención está alineada con requisitos de negocio/regulatorios explícitos?

Ver también: [`restore.md`](./restore.md), [`engine.md`](./engine.md), [`jobs.md`](./jobs.md).

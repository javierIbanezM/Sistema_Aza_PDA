# RDS (Relational Database Service)

> Ver [`../../decisions/rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md) para la comparativa con DynamoDB. Ver [`../sqlserver/`](../sqlserver/) para el conocimiento del motor SQL Server que RDS puede alojar gestionado.

## Qué es: motor relacional gestionado

RDS gestiona el aprovisionamiento, parcheo, backup y alta disponibilidad de motores relacionales estándar (PostgreSQL, MySQL, SQL Server, Oracle, MariaDB) — el equipo sigue diseñando esquema, índices y queries (ver [`../sqlserver/`](../sqlserver/) para SQL Server específicamente) igual que en una instancia autogestionada, pero sin la carga operativa de gestionar el motor en sí.

## Multi-AZ: alta disponibilidad, no escalado de lectura

```
Instancia primaria (AZ-a) ←── replicación síncrona ──→ Standby (AZ-b)
Failover automático ante fallo de la primaria, sin intervención manual
```

Multi-AZ mantiene una réplica standby síncrona para failover automático — **no** sirve tráfico de lectura (la standby no es accesible directamente); para escalar lecturas, usar Read Replicas por separado.

## Read Replicas: escalar lecturas, no alta disponibilidad automática

```
Primaria (escrituras) → replicación asíncrona → Read Replica 1, 2, 3 (solo lectura)
```

Las Read Replicas descargan tráfico de lectura de la instancia primaria — replicación asíncrona implica un lag de replicación (normalmente milisegundos, pero no garantizado), relevante si la aplicación lee inmediatamente después de escribir y necesita consistencia fuerte (en ese caso, leer de la primaria para esa operación específica).

## Backups automáticos y snapshots

- **Backups automáticos**: retención configurable (1-35 días), permiten restauración point-in-time — ver [`../sqlserver/backup.md`](../sqlserver/backup.md) para el mismo concepto a nivel de motor SQL Server.
- **Snapshots manuales**: retenidos indefinidamente hasta borrado explícito, útiles antes de un cambio de alto riesgo (migración de esquema mayor).

## Parameter Groups y Option Groups: configuración del motor

```
Parameter Group personalizado: max_connections=200, work_mem=64MB (PostgreSQL)
```

La configuración del motor (equivalente a los parámetros de configuración de una instancia autogestionada) se gestiona vía Parameter Groups — cambios que requieren reinicio deben planificarse en ventana de mantenimiento, igual que cualquier cambio de configuración de base de datos en producción.

## Checklist

- [ ] ¿Multi-AZ está habilitado para bases de datos de producción críticas, no confundido con escalado de lectura?
- [ ] ¿Las Read Replicas se usan para descargar lecturas, con tolerancia explícita al lag de replicación asíncrona?
- [ ] ¿La retención de backups automáticos corresponde al RPO real requerido por el negocio (ver [`../sqlserver/backup.md`](../sqlserver/backup.md))?
- [ ] ¿Existe un snapshot manual antes de cualquier cambio de alto riesgo?

Ver también: [`dynamodb.md`](./dynamodb.md), [`aurora.md`](./aurora.md), [`../../decisions/rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md), [`../sqlserver/`](../sqlserver/).

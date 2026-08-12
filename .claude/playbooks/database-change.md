# Playbook: Database Change

## Cuándo usar este playbook

Cualquier cambio de esquema de base de datos (nueva tabla, columna, índice, migración de datos, particionamiento) — especialmente relevante por el riesgo de indisponibilidad o pérdida de datos si se ejecuta sin planificación.

## Fases

### 1. Senior Business Analyst

- **Hace**: confirma el motivo de negocio del cambio y si existe una ventana de mantenimiento aceptable, o si el cambio debe ser sin downtime.
- **Produce**: restricciones de negocio (ventana disponible, tolerancia a downtime, urgencia).
- **Entrega a**: Software Architect.

### 2. Software Architect

- **Hace**: diseña el cambio de esquema siguiendo [`../standards/naming.md`](../standards/naming.md) y [`../standards/sql.md`](../standards/sql.md), evalúa si requiere particionamiento (ver [`../knowledge/sqlserver/partitioning.md`](../knowledge/sqlserver/partitioning.md)) o es un cambio simple, y diseña la estrategia de migración si hay datos existentes que transformar.
- **Produce**: diseño del cambio + estrategia de migración de datos si aplica + evaluación de si es compatible hacia atrás (¿el código actual sigue funcionando durante el despliegue gradual?).
- **Entrega a**: Tech Lead.

### 3. Tech Lead

- **Hace**: planifica el cambio en pasos seguros — para cambios sin downtime, típicamente: (1) añadir la columna/tabla nueva sin quitar la antigua, (2) desplegar código que escribe en ambas, (3) migrar datos históricos, (4) desplegar código que lee solo de la nueva, (5) eliminar la estructura antigua en un cambio posterior separado. Identifica el riesgo de bloqueo/indisponibilidad de cada paso (ver [`../knowledge/sqlserver/blocking.md`](../knowledge/sqlserver/blocking.md)).
- **Produce**: plan de migración por fases, con punto de rollback definido en cada una.
- **Entrega a**: [`../agents/senior-sqlserver-dba.md`](../agents/senior-sqlserver-dba.md).

### 4. Senior SQL Server DBA

- **Hace**: escribe la migración versionada (Flyway/Liquibase, ver [`../knowledge/hibernate/jpa.md`](../knowledge/hibernate/jpa.md)), siguiendo [`../recipes/sqlserver-create-index.md`](../recipes/sqlserver-create-index.md) si aplica, con batching para migraciones de datos masivas (ver [`../knowledge/sqlserver/performance.md`](../knowledge/sqlserver/performance.md)).
- **Produce**: script de migración versionado + plan de rollback probado en un entorno no productivo.
- **Entrega a**: Senior Code Reviewer.

### 5. Senior Code Reviewer

- **Hace**: revisa específicamente el riesgo de bloqueo/indisponibilidad de la migración, si el rollback es real y probado (no solo teórico), y si la migración es idempotente (se puede reejecutar sin duplicar efectos si falla a mitad).
- **Produce**: aprobación.

### 6. Tech Lead (ejecución coordinada)

- **Hace**: coordina la ejecución en la ventana acordada en la fase 1, con monitorización activa durante la migración (ver [`../knowledge/sqlserver/blocking.md`](../knowledge/sqlserver/blocking.md) para diagnóstico en tiempo real si algo se bloquea).
- **Produce**: migración ejecutada y verificada.
- **Terminada cuando**: los datos están consistentes, el rendimiento es el esperado (ver [`../knowledge/sqlserver/execution-plans.md`](../knowledge/sqlserver/execution-plans.md) para confirmar que los índices nuevos se usan), y no hay bloqueos residuales.

## Diagrama de flujo

```
Senior Business Analyst (restricciones de negocio: ventana, downtime)
        ↓
Software Architect (diseño del esquema + estrategia de migración)
        ↓
Tech Lead (plan por fases, con rollback en cada una)
        ↓
Senior SQL Server DBA (migración versionada + rollback probado)
        ↓
Senior Code Reviewer (riesgo de bloqueo + idempotencia + rollback real)
        ↓
Tech Lead (ejecución coordinada y monitorizada)
        ↓
   [Migración completada y verificada]
```

## Regla de oro de este playbook

Ninguna migración de esquema se ejecuta en producción sin haber probado el rollback en un entorno no productivo — un plan de rollback nunca probado no es un plan de rollback, es una suposición (mismo principio que [`../knowledge/sqlserver/restore.md`](../knowledge/sqlserver/restore.md) aplica a los simulacros de restauración).

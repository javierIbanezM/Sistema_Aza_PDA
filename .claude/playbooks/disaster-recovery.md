# Playbook: Disaster Recovery

## Cuándo usar este playbook

Un fallo catastrófico (región AWS completa caída, corrupción de datos extendida, pérdida total de un componente crítico) que excede lo que un rollback o mitigación estándar puede resolver — distinto de [`incident-response.md`](./incident-response.md), que cubre incidentes de severidad menor y más frecuentes.

## Fases

### 1. Senior AWS Architect + Senior DevOps Engineer (Incident Commander conjunto)

- **Hace**: evalúa el alcance real del desastre (¿es una región completa, un servicio específico, corrupción de datos?) y activa el plan de recuperación correspondiente, previamente diseñado y probado en simulacro (ver [`../knowledge/aws-database/rds.md`](../knowledge/aws-database/rds.md) para failover de base de datos, [`../knowledge/aws-network/route53.md`](../knowledge/aws-network/route53.md) para failover DNS a región secundaria).
- **Produce**: decisión de activar failover a región/entorno secundario, o restauración desde backup, con RTO/RPO ya conocidos de antemano (nunca calculados improvisadamente durante el desastre).
- **Entrega a**: Senior SQL Server DBA si la recuperación involucra restauración de base de datos.
- **Terminada cuando**: el sistema está operativo en el entorno de recuperación, aunque sea en modo degradado.

### 2. Senior SQL Server DBA (si aplica)

- **Hace**: ejecuta restauración point-in-time siguiendo [`../knowledge/sqlserver/restore.md`](../knowledge/sqlserver/restore.md), verificando la cadena de backups antes de restaurar.
- **Produce**: base de datos restaurada al punto más reciente posible dentro del RPO acordado.
- **Entrega a**: Senior DevOps Engineer para reconectar los servicios de aplicación.

### 3. Senior Business Analyst (en paralelo, desde el inicio)

- **Hace**: coordina comunicación a clientes/stakeholders sobre el alcance del desastre y el tiempo estimado de recuperación, gestiona implicaciones regulatorias si hay pérdida de datos.
- **Produce**: comunicación externa continua durante todo el evento.

### 4. Verificación de integridad tras la recuperación

- **Hace**: Senior Code Reviewer + Senior SQL Server DBA verifican consistencia de datos y funcionalidad completa antes de declarar el sistema recuperado, no solo "está respondiendo".
- **Produce**: confirmación de integridad, o identificación de datos/funcionalidad aún afectados.

### 5. Post-mortem extendido (obligatorio, con revisión del propio plan de DR)

- **Hace**: además del post-mortem estándar de [`incident-response.md`](./incident-response.md), se revisa específicamente si el plan de recuperación ante desastres funcionó como se diseñó — un simulacro nunca ejecutado no es un plan probado.
- **Produce**: actualización del plan de DR y calendario del próximo simulacro.

## Diagrama de flujo

```
Senior AWS Architect + Senior DevOps Engineer (evaluar alcance, activar plan de DR)
        │
        ├──→ Senior SQL Server DBA (restauración de datos, si aplica)
        └──→ Senior Business Analyst (comunicación continua)
        ↓
Verificación de integridad (Code Reviewer + DBA)
        ↓
   [Sistema recuperado, verificado]
        ↓
Post-mortem extendido + actualización del plan de DR
```

## Validaciones obligatorias

- El plan de recuperación activado fue probado previamente en un simulacro, nunca improvisado por primera vez durante el desastre real.
- RTO/RPO conocidos de antemano, no estimados durante el evento.
- Verificación de integridad de datos antes de declarar el sistema recuperado, no solo disponibilidad superficial.

## Criterios de finalización

El evento se considera cerrado cuando el sistema está completamente operativo con integridad de datos verificada, la comunicación a stakeholders está completa, y el post-mortem extendido (incluida la revisión del propio plan de DR) está documentado con el simulacro siguiente ya calendarizado.

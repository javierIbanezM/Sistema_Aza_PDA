# Pitfalls (DevOps) — Errores frecuentes

## Desplegar manualmente "por esta vez"

```
Un despliegue fuera del pipeline rompe la trazabilidad y suele repetirse "solo esta vez" indefinidamente
```

Ver [`ci-cd.md`](./ci-cd.md).

## Tag de imagen `latest` en producción

```dockerfile
image: pedidos-service:latest  # qué versión exacta está corriendo es ambiguo, no reproducible
```

Ver [`artifact-management.md`](./artifact-management.md).

## Migración de esquema contractiva sin fase expansiva previa

```sql
ALTER TABLE pedidos DROP COLUMN estado_legacy;  -- si hay que revertir el código, el rollback ya no es posible
```

Ver [`rollback.md`](./rollback.md).

## Staging con configuración/versión distinta a producción

```
Staging con una versión de base de datos distinta "porque es más barato" — un bug específico
de la versión de producción nunca se detecta antes del despliegue real
```

Ver [`environments.md`](./environments.md).

## Secretos embebidos en la imagen de contenedor

```dockerfile
COPY .env .env  # el secreto persiste en la capa de la imagen aunque se "elimine" después
```

Ver [`secrets-management.md`](./secrets-management.md), [`containerization.md`](./containerization.md).

## Alertas sin runbook, generando fatiga de alertas

```
Alerta disparándose constantemente sin acción clara → el equipo aprende a ignorarla,
incluida la próxima vez que sea genuinamente crítica
```

Ver [`alerting.md`](./alerting.md).

## Checklist de revisión rápida

- [ ] ¿Existe algún despliegue manual fuera del pipeline?
- [ ] ¿Se usa `latest` como tag de imagen en producción?
- [ ] ¿Alguna migración es contractiva sin haber pasado primero por una fase expansiva?
- [ ] ¿Hay secretos embebidos en alguna imagen de contenedor?

Ver también: [`best-practices.md`](./best-practices.md).

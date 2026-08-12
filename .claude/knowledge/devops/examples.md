# Examples (DevOps)

## Ejemplo 1 — Diseñar el pipeline completo de un nuevo servicio

**Contexto**: nuevo microservicio Java necesita CI/CD desde cero.

**Flujo aplicado**: Lint → Build → Unit Tests → SAST → build de imagen multi-stage (ver [`containerization.md`](./containerization.md)) → escaneo de vulnerabilidades (ver [`artifact-management.md`](./artifact-management.md)) → publicación con tag = SHA de commit → despliegue a staging con Rolling Update (ver [`deployment-strategies.md`](./deployment-strategies.md)) → E2E tests → aprobación manual → despliegue a producción → smoke tests → observabilidad instrumentada desde el primer despliegue (ver [`observability.md`](./observability.md)). Ver [`../../recipes/create-github-actions-pipeline.md`](../../recipes/create-github-actions-pipeline.md) para la implementación completa.

---

## Ejemplo 2 — Despliegue Canary de una funcionalidad de alto riesgo

**Contexto**: cambio en el algoritmo de cálculo de precios, alto riesgo de negocio si falla.

**Flujo aplicado**: ver [`../../decisions/blue-green-vs-canary.md`](../../decisions/blue-green-vs-canary.md) — se elige Canary sobre Blue/Green por la necesidad de exposición gradual con comparación de métricas de negocio (no solo técnicas) entre versión estable y candidata. 5% de tráfico a la nueva versión durante 2 horas, monitorizando específicamente la métrica de negocio "total medio por pedido" además de los golden signals técnicos (ver [`monitoring.md`](./monitoring.md)) — cualquier desviación significativa dispara rollback automático (ver [`rollback.md`](./rollback.md)) antes de aumentar el porcentaje.

---

## Ejemplo 3 — Incidente de producción con rollback y post-mortem

**Contexto**: un despliegue introduce un error 500 en el 10% de las peticiones, detectado por alerta automática.

**Flujo aplicado**, siguiendo [`../../playbooks/incident-response.md`](../../playbooks/incident-response.md): la alerta (ver [`alerting.md`](./alerting.md)) tiene severidad crítica y runbook vinculado, notificando al on-call inmediatamente. Se confirma con [`observability.md`](./observability.md) que el problema coincide temporalmente con el último despliegue — se ejecuta rollback siguiendo [`../../playbooks/rollback-production.md`](../../playbooks/rollback-production.md), previamente probado en simulacro (ver [`rollback.md`](./rollback.md)). Tras estabilizar, post-mortem que identifica que faltaba un test de integración para el caso concreto que falló — se añade antes de reintentar el despliegue.

---

## Ejemplo 4 — Migrar la infraestructura de un servicio a IaC desde gestión manual

**Contexto**: un servicio antiguo tiene su infraestructura creada manualmente en la consola AWS, sin código.

**Flujo aplicado**: se usa `terraform import` para traer los recursos existentes bajo gestión de Terraform sin recrearlos (ver [`infrastructure-as-code.md`](./infrastructure-as-code.md) y [`../terraform/terraform.md`](../terraform/terraform.md)), verificando con `terraform plan` que no hay diferencias entre el estado real y el código antes de considerar la migración completa — cualquier diferencia detectada se investiga (¿era un cambio manual no documentado?) antes de aplicar nada, para no alterar accidentalmente infraestructura de producción durante la migración.

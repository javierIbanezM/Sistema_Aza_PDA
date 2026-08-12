# Knowledge — DevOps

Índice del dominio DevOps: proceso de entrega continua, estrategias de despliegue, observabilidad de plataforma, gestión de infraestructura/configuración/secretos. Herramientas específicas (Docker, Kubernetes, Terraform, GitHub Actions) tienen su propia carpeta — este dominio cubre la disciplina de ingeniería, no la sintaxis de cada herramienta.

| Documento | Descripción |
|---|---|
| [`ci-cd.md`](./ci-cd.md) | Pipeline como única vía a producción, etapas, builds reproducibles |
| [`deployment-strategies.md`](./deployment-strategies.md) | Rolling, Blue/Green, Canary, Feature Flags |
| [`release-management.md`](./release-management.md) | Versionado semántico, changelog, trazabilidad de releases |
| [`rollback.md`](./rollback.md) | Rollback probado, migraciones expansivas/contractivas, rollback automático |
| [`infrastructure-as-code.md`](./infrastructure-as-code.md) | Idempotencia, módulos reutilizables, estado remoto |
| [`observability.md`](./observability.md) | Estrategia de plataforma que conecta logs/métricas/trazas |
| [`monitoring.md`](./monitoring.md) | Métricas de infraestructura compartida, dashboards por audiencia |
| [`logging.md`](./logging.md) | Pipeline de agregación de logs, retención escalonada |
| [`tracing.md`](./tracing.md) | Colector de trazas, sampling head vs. tail-based |
| [`alerting.md`](./alerting.md) | Severidad, runbooks, fatiga de alertas, escalado |
| [`environments.md`](./environments.md) | Paridad, aislamiento, anonimización de datos de prueba |
| [`configuration-management.md`](./configuration-management.md) | Configuración externalizada, jerarquía de precedencia |
| [`artifact-management.md`](./artifact-management.md) | Inmutabilidad, escaneo de vulnerabilidades, retención |
| [`containerization.md`](./containerization.md) | Multi-stage builds, usuario no privilegiado, cacheo de capas |
| [`orchestration.md`](./orchestration.md) | Requests/limits, Readiness/Liveness, autoescalado |
| [`secrets-management.md`](./secrets-management.md) | Ciclo de vida de secretos en pipeline y despliegue |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes del dominio |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos de DevOps |
| [`examples.md`](./examples.md) | Casos de uso completos |

## Dominios de herramientas relacionados (no duplicados aquí)

| Herramienta | Carpeta |
|---|---|
| Docker | [`../docker/`](../docker/) |
| Kubernetes | [`../kubernetes/`](../kubernetes/) |
| Terraform | [`../terraform/`](../terraform/) |
| GitHub Actions | [`../github-actions/`](../github-actions/) |

## Cómo usar este índice

- Para observabilidad a nivel de **aplicación** (cómo instrumentar, no cómo agregar), ver [`../architecture/logging.md`](../architecture/logging.md) e [`../integration/observability.md`](../integration/observability.md) — no duplicado aquí.
- Para servicios AWS concretos (CloudWatch, ECS, IAM), ver los dominios `aws-*` — este dominio los referencia sin repetirlos.
- Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí ni en otro dominio (regla de oro nº2).

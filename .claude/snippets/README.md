# Snippets — Índice

Fragmentos de código **reutilizables y autocontenidos**, sin explicación ni razonamiento — a diferencia de `recipes/` (que son procesos paso a paso con contexto) y `knowledge/` (que explica y razona), un snippet es directamente **código que se copia y adapta**.

## Diferencia con `recipes/`

| Carpeta | Contenido |
|---|---|
| `recipes/` | Proceso paso a paso con explicación de cada paso, enlaces a `knowledge/`/`standards/`, checklist final |
| `snippets/` | Solo el código, listo para insertar, con el mínimo de comentarios necesarios para adaptarlo |

## Estructura

```
snippets/<tecnologia>/<nombre>.<extension>
```

## Java

| Snippet | Contenido |
|---|---|
| [`java/builder.java`](./java/builder.java) | Builder pattern con Lombok |
| [`java/repository.java`](./java/repository.java) | Interfaz de Repository + implementación Spring Data JPA |
| [`java/service.java`](./java/service.java) | Caso de uso con inyección por constructor |
| [`java/controller.java`](./java/controller.java) | REST Controller con manejo de errores |

## SQL Server

| Snippet | Contenido |
|---|---|
| [`sqlserver/trigger.sql`](./sqlserver/trigger.sql) | Trigger de auditoría |
| [`sqlserver/store.sql`](./sqlserver/store.sql) | Stored procedure con manejo de errores |
| [`sqlserver/merge.sql`](./sqlserver/merge.sql) | Patrón MERGE (upsert) + alternativa segura bajo concurrencia |

## Node.js

| Snippet | Contenido |
|---|---|
| [`node/controller.ts`](./node/controller.ts) | Controller NestJS con paginación |
| [`node/service.ts`](./node/service.ts) | Caso de uso con evento desacoplado |
| [`node/middleware.ts`](./node/middleware.ts) | Middleware de correlación + manejo global de errores |

## JavaScript

| Snippet | Contenido |
|---|---|
| [`javascript/fetch-client.js`](./javascript/fetch-client.js) | Cliente fetch con timeout, verificación y reintentos |
| [`javascript/event-cleanup.js`](./javascript/event-cleanup.js) | Limpieza simétrica de listeners |

## Android

| Snippet | Contenido |
|---|---|
| [`android/compose-screen.kt`](./android/compose-screen.kt) | Pantalla Compose + ViewModel + StateFlow |
| [`android/room-dao.kt`](./android/room-dao.kt) | Entity + DAO + Database + módulo Hilt |
| [`android/retrofit-service.kt`](./android/retrofit-service.kt) | Servicio Retrofit + Repository cache-aside |

## Integration

| Snippet | Contenido |
|---|---|
| [`integration/idempotency-filter.java`](./integration/idempotency-filter.java) | Servicio de verificación de `Idempotency-Key` |
| [`integration/resilient-client.java`](./integration/resilient-client.java) | Cliente con Circuit Breaker + reintentos + traducción de errores |

## AWS

| Snippet | Contenido |
|---|---|
| [`aws/lambda-terraform.tf`](./aws/lambda-terraform.tf) | Lambda con IAM mínimo, trigger SQS + DLQ, X-Ray |
| [`aws/ecs-service-terraform.tf`](./aws/ecs-service-terraform.tf) | Task Definition + Service + Auto Scaling ECS Fargate |
| [`aws/vpc-terraform.tf`](./aws/vpc-terraform.tf) | VPC con subredes públicas/privadas y Security Groups en cadena |

## DevOps

| Snippet | Contenido |
|---|---|
| [`devops/Dockerfile`](./devops/Dockerfile) | Multi-stage build con usuario no privilegiado |
| [`devops/docker-compose.yml`](./devops/docker-compose.yml) | Entorno local con healthchecks y secretos externalizados |
| [`devops/deployment.yaml`](./devops/deployment.yaml) | Deployment de Kubernetes con requests/limits y probes |
| [`devops/service.yaml`](./devops/service.yaml) | Service + Ingress + NetworkPolicy |
| [`devops/github-actions.yml`](./devops/github-actions.yml) | Pipeline con OIDC, escaneo y aprobación de producción |
| [`devops/terraform.tf`](./devops/terraform.tf) | Backend remoto + módulos reutilizables |
| [`devops/helm-values.yaml`](./devops/helm-values.yaml) | Values.yaml para instalar dependencias de terceros |

## Security

| Snippet | Contenido |
|---|---|
| [`security/security-headers.java`](./security/security-headers.java) | Cabeceras de seguridad HTTP (CSP, HSTS, X-Frame-Options) |
| [`security/dependency-scan.yml`](./security/dependency-scan.yml) | Escaneo de dependencias bloqueante + SBOM + re-escaneo periódico |

## Performance

| Snippet | Contenido |
|---|---|
| [`performance/load-test.js`](./performance/load-test.js) | Script k6 con rampa, estado estable y thresholds = performance budget |

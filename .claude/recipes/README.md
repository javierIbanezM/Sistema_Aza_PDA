# Recipes — Índice

Instrucciones **paso a paso, accionables y concretas** para tareas técnicas recurrentes (a diferencia de `playbooks/`, que son procesos de negocio/proceso multi-dominio como "nueva funcionalidad" o "hotfix"). Una recipe responde a "¿cómo creo X en este stack, ahora mismo?" con los pasos exactos, aplicando ya las reglas de `knowledge/`, `patterns/` y `standards/` correspondientes.

## Convención de nombres

Todas las recipes viven **planas** directamente en `recipes/` (sin subcarpetas por tecnología), con el nombre de la tecnología como prefijo: `<tecnologia>-<accion>.md`. Esto permite encontrarlas por búsqueda directa de texto en cualquier editor, sin navegar por carpetas.

## Java

| Recipe | Cuándo usarla |
|---|---|
| [`java-create-rest-controller.md`](./java-create-rest-controller.md) | Exponer un nuevo endpoint HTTP completo (DTO, mapper, controller, errores, tests) |
| [`java-create-service.md`](./java-create-service.md) | Crear un nuevo caso de uso de la capa de aplicación |
| [`java-create-repository.md`](./java-create-repository.md) | Crear un nuevo Repository con Spring Data JPA |
| [`java-create-worker.md`](./java-create-worker.md) | Crear un consumidor de cola (Kafka/RabbitMQ) idempotente |
| [`java-create-scheduler.md`](./java-create-scheduler.md) | Crear una tarea programada (`@Scheduled`) segura en despliegues multi-réplica |
| [`java-create-jpa-entity.md`](./java-create-jpa-entity.md) | Crear una nueva entidad JPA a partir de una entidad de dominio ya diseñada |

## SQL Server

| Recipe | Cuándo usarla |
|---|---|
| [`sqlserver-create-trigger.md`](./sqlserver-create-trigger.md) | Crear un trigger de auditoría (nunca lógica de negocio) |
| [`sqlserver-create-store-procedure.md`](./sqlserver-create-store-procedure.md) | Crear un procedimiento almacenado con manejo de errores |
| [`sqlserver-create-index.md`](./sqlserver-create-index.md) | Crear un índice basado en un problema de rendimiento confirmado |
| [`sqlserver-optimize-query.md`](./sqlserver-optimize-query.md) | Diagnosticar y optimizar una query lenta paso a paso |

## Node.js

| Recipe | Cuándo usarla |
|---|---|
| [`node-create-rest-endpoint.md`](./node-create-rest-endpoint.md) | Exponer un nuevo endpoint HTTP completo con NestJS |
| [`node-create-service.md`](./node-create-service.md) | Crear un nuevo caso de uso desacoplado con eventos |
| [`node-create-middleware.md`](./node-create-middleware.md) | Crear un middleware Express con manejo de errores correcto |
| [`node-create-websocket.md`](./node-create-websocket.md) | Crear un canal WebSocket autenticado y autorizado |

## JavaScript

| Recipe | Cuándo usarla |
|---|---|
| [`js-create-fetch-client.md`](./js-create-fetch-client.md) | Crear un cliente fetch robusto con timeout, verificación y reintentos |
| [`js-fix-memory-leak.md`](./js-fix-memory-leak.md) | Diagnosticar y corregir una fuga de memoria por listeners no removidos |

## Android

| Recipe | Cuándo usarla |
|---|---|
| [`android-create-compose-screen.md`](./android-create-compose-screen.md) | Crear una pantalla completa: estado UI, ViewModel, Composable, tests |
| [`android-create-room-database.md`](./android-create-room-database.md) | Crear una base de datos Room a partir de un dominio ya diseñado |
| [`android-create-webview.md`](./android-create-webview.md) | Integrar un WebView de forma segura (whitelist, JS interface mínima) |
| [`android-create-login.md`](./android-create-login.md) | Pantalla de login con almacenamiento seguro de tokens y refresh automático |

## Integration

| Recipe | Cuándo usarla |
|---|---|
| [`integration-add-idempotency.md`](./integration-add-idempotency.md) | Añadir soporte de `Idempotency-Key` a un endpoint |
| [`integration-create-resilient-client.md`](./integration-create-resilient-client.md) | Crear un cliente resiliente para un sistema externo (ERP/WMS/CRM) |
| [`integration-create-saga.md`](./integration-create-saga.md) | Implementar una Saga orquestada entre múltiples sistemas |

## AWS

| Recipe | Cuándo usarla |
|---|---|
| [`aws-create-lambda.md`](./aws-create-lambda.md) | Crear una función Lambda con IAM mínimo, idempotencia y trigger SQS |
| [`aws-deploy-ecs-service.md`](./aws-deploy-ecs-service.md) | Desplegar un servicio en ECS Fargate con Auto Scaling |
| [`aws-setup-vpc.md`](./aws-setup-vpc.md) | Configurar una VPC segura con subredes públicas/privadas |

## DevOps

| Recipe | Cuándo usarla |
|---|---|
| [`deploy-docker-application.md`](./deploy-docker-application.md) | Construir y desplegar una imagen Docker con buenas prácticas de seguridad |
| [`deploy-kubernetes-application.md`](./deploy-kubernetes-application.md) | Desplegar una aplicación completa en Kubernetes |
| [`create-github-actions-pipeline.md`](./create-github-actions-pipeline.md) | Crear un pipeline CI/CD completo con GitHub Actions |
| [`terraform-deploy-aws.md`](./terraform-deploy-aws.md) | Desplegar infraestructura AWS como código con Terraform |
| [`rollback-production.md`](./rollback-production.md) | Ejecutar un rollback verificado en producción |
| [`blue-green-deployment.md`](./blue-green-deployment.md) | Desplegar con conmutación atómica de tráfico |
| [`canary-deployment.md`](./canary-deployment.md) | Desplegar con exposición gradual y monitorización activa |

## Security

| Recipe | Cuándo usarla |
|---|---|
| [`security-threat-model.md`](./security-threat-model.md) | Conducir una sesión de threat modeling con STRIDE |
| [`security-scan-dependencies.md`](./security-scan-dependencies.md) | Configurar escaneo de dependencias bloqueante y SBOM |
| [`security-review-architecture.md`](./security-review-architecture.md) | Ejecutar una revisión de arquitectura segura ordenada |

## Performance

| Recipe | Cuándo usarla |
|---|---|
| [`performance-run-load-test.md`](./performance-run-load-test.md) | Ejecutar un ciclo completo de pruebas de carga (Load/Stress/Spike/Soak) |

## Próximos dominios (se añaden en sus milestones correspondientes)

```
(ninguno pendiente actualmente)
```

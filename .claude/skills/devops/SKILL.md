# Skill: DevOps

## Rol

Senior DevOps Engineer. Guía diseño de pipelines, estrategias de despliegue, infraestructura como código, observabilidad de plataforma y gestión de incidentes — nunca un catálogo de herramientas, siempre decisiones de ingeniería con trade-offs explícitos.

## Cuándo usar / no usar

**Usar** ante: diseño/modificación de pipeline CI/CD, elección de estrategia de despliegue, infraestructura como código, contenerización/orquestación, gestión de secretos/configuración/entornos, observabilidad de plataforma, rollback, gestión de incidentes.

**No usar** ante: decisiones de infraestructura AWS específicas de servicio (ir a `skills/aws/`), lógica de negocio de la aplicación (ir a la Skill técnica del lenguaje), integración entre sistemas no relacionada con pipeline/despliegue (ir a `skills/integration/`).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. Consultar `knowledge/devops/README.md` para el índice del dominio.
3. Si hay una decisión entre dos herramientas/estrategias, consultar primero `decisions/` — probablemente ya está resuelta.
4. Consultar el documento específico (`ci-cd.md`, `deployment-strategies.md`, `rollback.md`...) y, si aplica, el dominio de herramienta (`knowledge/docker/`, `knowledge/kubernetes/`, `knowledge/terraform/`, `knowledge/github-actions/`).
5. Usar la `recipes/*.md` correspondiente si la tarea coincide con una receta ya definida.
6. Para procesos completos (despliegue, rollback, incidente), usar el `playbooks/*.md` correspondiente.
7. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

[`knowledge/devops/`](../../knowledge/devops/) — 20 documentos (ver README). [`knowledge/docker/`](../../knowledge/docker/), [`knowledge/kubernetes/`](../../knowledge/kubernetes/), [`knowledge/terraform/`](../../knowledge/terraform/), [`knowledge/github-actions/`](../../knowledge/github-actions/) para herramientas específicas. [`knowledge/aws-*/`](../../knowledge/) para infraestructura AWS. No duplicado aquí.

## Decisions relevantes

[`decisions/`](../../decisions/): `docker-compose-vs-kubernetes.md`, `terraform-vs-cloudformation.md`, `github-actions-vs-jenkins.md`, `blue-green-vs-canary.md`, `rolling-vs-blue-green.md`, `monorepo-vs-multirepo.md`, `push-vs-pull-deployment.md`, `helm-vs-kustomize.md`.

## Patrones y anti-patrones

[`patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md). [`anti-patterns/`](../../anti-patterns/): `god-object.md` (pipelines monolíticos sin modularidad).

## Standards a aplicar

[`standards/git.md`](../../standards/git.md), [`standards/commits.md`](../../standards/commits.md), [`standards/logging-standard.md`](../../standards/logging-standard.md).

## Checklists

[`checklists/architecture.md`](../../checklists/architecture.md).

## Recipes a utilizar

[`recipes/`](../../recipes/): `deploy-docker-application.md`, `deploy-kubernetes-application.md`, `create-github-actions-pipeline.md`, `terraform-deploy-aws.md`, `rollback-production.md`, `blue-green-deployment.md`, `canary-deployment.md`.

## Snippets a reutilizar

[`snippets/devops/`](../../snippets/devops/).

## Playbooks a ejecutar

[`playbooks/`](../../playbooks/): `deploy-production.md`, `rollback-production.md`, `release-management.md`, `incident-response.md`, `disaster-recovery.md`.

## Ejemplos

[`knowledge/devops/examples.md`](../../knowledge/devops/examples.md).

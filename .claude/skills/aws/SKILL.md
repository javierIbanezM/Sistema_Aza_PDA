# Skill: AWS

## Rol

Senior AWS Architect. Guía decisiones de infraestructura y servicios en AWS — no describe servicios individualmente, **decide entre alternativas** (EC2 vs. Lambda, RDS vs. DynamoDB, ECS vs. EKS...) según el problema real, aplicando siempre primero las reglas transversales de `architecture`.

## Cuándo usar / no usar

**Usar** ante: diseño de infraestructura en AWS, elección de servicio de cómputo/almacenamiento/base de datos/red/mensajería, configuración de seguridad IAM/KMS, observabilidad con CloudWatch/X-Ray.

**No usar** ante: decisiones de arquitectura de aplicación no específicas de AWS (ir a `skills/architecture/`), lógica de negocio interna (ir a la Skill técnica del lenguaje correspondiente), integración entre sistemas no relacionada con servicios AWS concretos (ir a `skills/integration/`).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. Identificar el dominio funcional (Compute/Storage/Database/Network/Security/Monitoring/Integration) y cargar `knowledge/aws-<dominio>/README.md`.
3. Si hay una decisión entre dos servicios, consultar primero `decisions/` — probablemente ya está resuelta.
4. Consultar el documento específico del servicio.
5. Aplicar `standards/` y el principio de menor privilegio de `knowledge/aws-security/iam.md` en cualquier configuración.
6. Usar la `recipes/aws-*.md` correspondiente si la tarea coincide con una receta ya definida.
7. Implementar como código (Terraform), nunca manualmente en la consola para infraestructura de equipo (ver `CLAUDE.md` sección 8.1).
8. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

[`knowledge/aws/README.md`](../../knowledge/aws/README.md) como índice general de los 7 dominios: [`knowledge/aws-compute/`](../../knowledge/aws-compute/), [`knowledge/aws-storage/`](../../knowledge/aws-storage/), [`knowledge/aws-database/`](../../knowledge/aws-database/), [`knowledge/aws-network/`](../../knowledge/aws-network/), [`knowledge/aws-security/`](../../knowledge/aws-security/), [`knowledge/aws-monitoring/`](../../knowledge/aws-monitoring/), [`knowledge/aws-integration/`](../../knowledge/aws-integration/).

## Decisions relevantes (el núcleo del valor de esta Skill)

[`decisions/`](../../decisions/): `ec2-vs-lambda.md`, `rds-vs-dynamodb.md`, `ecs-vs-eks.md`, `sqs-vs-eventbridge.md`, `cloudfront-vs-alb.md`, `s3-vs-efs.md`, `secrets-manager-vs-parameter-store.md`.

## Patrones a consultar

[`patterns/`](../../patterns/) (global): `circuit-breaker.md`, `adapter.md`.

## Anti-patrones a consultar

[`anti-patterns/`](../../anti-patterns/) (global): `memory-leak.md` (recursos AWS no liberados/desatendidos), `god-object.md`.

## Standards a aplicar

[`standards/`](../../standards/) (global): `naming.md`, `logging-standard.md`, `exceptions.md`.

## Checklists a ejecutar

[`checklists/architecture.md`](../../checklists/architecture.md).

## Prompts a utilizar

[`prompts/`](../../prompts/) (global): `review.md`, `performance.md`.

## Recipes a utilizar

[`recipes/`](../../recipes/) (plano, prefijo `aws-`): `aws-create-lambda.md`, `aws-deploy-ecs-service.md`, `aws-setup-vpc.md`.

## Snippets a reutilizar

[`snippets/aws/`](../../snippets/aws/).

## Ejemplos

Ver los ejemplos embebidos en cada documento de `knowledge/aws-*/` y en las recipes — este dominio no tiene un `examples.md` centralizado dado el número de dominios funcionales independientes.

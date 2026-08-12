# Knowledge — AWS (índice general)

AWS se divide en **dominios funcionales independientes**, no en un único `knowledge/aws/` monolítico — con más de 200 servicios, agrupar por función (Compute, Storage, Database, Network, Security, Monitoring, Integration) mantiene cada dominio navegable a medida que el framework crece, siguiendo la misma filosofía de separación aplicada a Java/Spring/Hibernate o JavaScript/Node.

## Dominios

| Dominio | Servicios cubiertos |
|---|---|
| [`../aws-compute/`](../aws-compute/) | EC2, Lambda, ECS, EKS, Batch |
| [`../aws-storage/`](../aws-storage/) | S3, EFS, FSx |
| [`../aws-database/`](../aws-database/) | RDS, DynamoDB, Aurora, ElastiCache |
| [`../aws-network/`](../aws-network/) | VPC, ALB, NLB, Route 53, CloudFront |
| [`../aws-security/`](../aws-security/) | IAM, KMS, Cognito, Secrets Manager |
| [`../aws-monitoring/`](../aws-monitoring/) | CloudWatch, CloudTrail, X-Ray |
| [`../aws-integration/`](../aws-integration/) | SQS, SNS, EventBridge, API Gateway |

## Comparativas de decisión (dónde vive el verdadero valor arquitectónico)

Ver [`../../decisions/`](../../decisions/): [`ec2-vs-lambda.md`](../../decisions/ec2-vs-lambda.md), [`rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md), [`ecs-vs-eks.md`](../../decisions/ecs-vs-eks.md), [`sqs-vs-eventbridge.md`](../../decisions/sqs-vs-eventbridge.md), [`cloudfront-vs-alb.md`](../../decisions/cloudfront-vs-alb.md), [`s3-vs-efs.md`](../../decisions/s3-vs-efs.md), [`secrets-manager-vs-parameter-store.md`](../../decisions/secrets-manager-vs-parameter-store.md).

Elegir el servicio correcto para un problema concreto aporta más valor que memorizar las características de cada servicio individualmente — este es el ángulo que adopta [`../../agents/senior-aws-architect.md`](../../agents/senior-aws-architect.md).

## Cómo usar este índice

- Antes de crear un documento nuevo en cualquier dominio `aws-*`, comprueba que el servicio no esté ya cubierto (regla de oro nº2).
- Un servicio nuevo se añade al dominio funcional correspondiente, nunca a un `knowledge/aws/` general — si no encaja claramente en los 7 dominios existentes, es señal de que podría necesitarse un dominio funcional nuevo, no de que deba forzarse en uno existente.
- Para SQL Server específicamente alojado en RDS, ver [`../sqlserver/`](../sqlserver/) — el motor en sí no se duplica aquí, solo se referencia desde [`../aws-database/rds.md`](../aws-database/rds.md).

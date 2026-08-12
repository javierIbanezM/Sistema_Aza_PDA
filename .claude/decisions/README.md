# Decisions — Índice

Comparativas técnicas justificadas entre dos o más enfoques que compiten para resolver el mismo problema. A diferencia de un ADR (`templates/ADR.md`, que documenta una decisión **ya tomada** en un proyecto concreto), un documento de `decisions/` es **conocimiento reutilizable de comparación**, consultado antes de tomar la decisión en cualquier proyecto — el ADR resultante puede citar el documento de `decisions/` correspondiente en lugar de repetir el análisis.

## Formato de cada documento

- Contexto: qué problema resuelven ambas opciones.
- Comparativa: tabla de criterios relevantes.
- Cuándo elegir cada opción.
- Recomendación por defecto de este framework (si existe una razonable).

## Contenido

| Documento | Comparativa |
|---|---|
| [`repository-vs-jdbc.md`](./repository-vs-jdbc.md) | Repository pattern + ORM vs. JDBC directo |
| [`jpa-vs-mybatis.md`](./jpa-vs-mybatis.md) | JPA/Hibernate vs. MyBatis |
| [`rest-vs-graphql.md`](./rest-vs-graphql.md) | API REST vs. GraphQL |
| [`jwt-vs-session.md`](./jwt-vs-session.md) | Autenticación con JWT vs. sesiones de servidor |
| [`ec2-vs-lambda.md`](./ec2-vs-lambda.md) | Cómputo en instancias vs. serverless |
| [`rds-vs-dynamodb.md`](./rds-vs-dynamodb.md) | Persistencia relacional vs. NoSQL gestionada |
| [`ecs-vs-eks.md`](./ecs-vs-eks.md) | Orquestación de contenedores propietaria vs. Kubernetes gestionado |
| [`sqs-vs-eventbridge.md`](./sqs-vs-eventbridge.md) | Cola simple vs. bus de eventos con enrutamiento por contenido |
| [`cloudfront-vs-alb.md`](./cloudfront-vs-alb.md) | CDN vs. balanceador de carga regional |
| [`s3-vs-efs.md`](./s3-vs-efs.md) | Almacenamiento de objetos vs. sistema de archivos POSIX |
| [`secrets-manager-vs-parameter-store.md`](./secrets-manager-vs-parameter-store.md) | Secretos con rotación automática vs. configuración general |
| [`docker-compose-vs-kubernetes.md`](./docker-compose-vs-kubernetes.md) | Orquestación local de un host vs. producción multi-nodo |
| [`terraform-vs-cloudformation.md`](./terraform-vs-cloudformation.md) | IaC multi-cloud vs. nativo de AWS |
| [`github-actions-vs-jenkins.md`](./github-actions-vs-jenkins.md) | CI/CD SaaS integrado vs. autogestionado agnóstico |
| [`blue-green-vs-canary.md`](./blue-green-vs-canary.md) | Conmutación instantánea vs. exposición gradual de tráfico |
| [`rolling-vs-blue-green.md`](./rolling-vs-blue-green.md) | Reemplazo incremental vs. conmutación atómica |
| [`monorepo-vs-multirepo.md`](./monorepo-vs-multirepo.md) | Un repositorio para todo vs. un repositorio por servicio |
| [`push-vs-pull-deployment.md`](./push-vs-pull-deployment.md) | CI despliega directamente vs. GitOps con reconciliación continua |
| [`helm-vs-kustomize.md`](./helm-vs-kustomize.md) | Plantillas parametrizadas vs. superposiciones declarativas de manifiestos |
| [`sast-vs-dast.md`](./sast-vs-dast.md) | Análisis estático de código vs. dinámico de la aplicación en ejecución |
| [`horizontal-vs-vertical-scaling.md`](./horizontal-vs-vertical-scaling.md) | Más instancias vs. instancias más grandes |

## Próximas comparativas (se añaden a medida que los dominios correspondientes lo justifiquen)

```
sql-vs-nosql.md         (Bases de datos — general)
```

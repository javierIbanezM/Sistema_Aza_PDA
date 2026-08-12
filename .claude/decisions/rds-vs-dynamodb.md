# RDS vs. DynamoDB

## Contexto

Ambos son opciones de persistencia gestionadas en AWS, con modelos de datos y de escalado fundamentalmente distintos — la elección entre ambos es, en el fondo, la misma decisión que [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md) y [`repository-vs-jdbc.md`](./repository-vs-jdbc.md) tratan a nivel de framework, aplicada al contexto gestionado de AWS.

## Comparativa

| Criterio | RDS (relacional) | DynamoDB (NoSQL clave-valor) |
|---|---|---|
| Modelo de datos | Relacional, normalizado, consultas ad-hoc flexibles (SQL) | Diseñado por patrón de acceso predefinido, consultas limitadas a la clave/índices diseñados |
| Escalado de escritura | Vertical principalmente (mayor instancia), limitado | Horizontal prácticamente ilimitado, automático |
| Consistencia | Fuerte, transaccional (ACID) de forma nativa | Eventual por defecto, fuerte opcional con coste (ver [`../knowledge/aws-database/dynamodb.md`](../knowledge/aws-database/dynamodb.md)) |
| Flexibilidad de consulta | Alta — JOINs, agregaciones, consultas no anticipadas | Baja — requiere anticipar el patrón de acceso en el diseño del esquema |
| Latencia a gran escala | Puede degradarse sin particionamiento (ver [`../knowledge/sqlserver/partitioning.md`](../knowledge/sqlserver/partitioning.md)) | Latencia de milisegundos consistente independientemente de la escala |
| Curva de aprendizaje del equipo | Baja si el equipo ya domina SQL (la mayoría) | Requiere repensar el modelado de datos por patrón de acceso |

## Cuándo elegir RDS

- El dominio requiere consultas relacionales flexibles, reportes ad-hoc, o JOINs entre múltiples entidades no anticipables completamente en el diseño inicial.
- Transacciones ACID multi-tabla son un requisito genuino del dominio (ver [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md), Agregados que abarcan varias tablas).
- El equipo ya tiene fuerte experiencia SQL (ver [`../knowledge/sqlserver/`](../knowledge/sqlserver/)) y el volumen no exige escalado horizontal extremo.

## Cuándo elegir DynamoDB

- El patrón de acceso está bien definido y es estable (se sabe de antemano cómo se va a consultar el dato).
- Se necesita escalado horizontal prácticamente ilimitado con latencia de milisegundos consistente, sin la complejidad operativa de particionar manualmente una base relacional.
- El dominio encaja naturalmente en un modelo de agregados independientes sin necesidad de JOINs entre entidades no relacionadas directamente.

## Recomendación por defecto de este framework

**RDS por defecto** para el dominio transaccional principal de una aplicación nueva, alineado con el enfoque relacional que el framework ya documenta en profundidad (ver [`../knowledge/sqlserver/`](../knowledge/sqlserver/)); **DynamoDB** para casos específicos con patrón de acceso bien definido y requisito genuino de escalado horizontal extremo (tablas de sesión, contadores de alta frecuencia, catálogos de solo lectura de gran volumen) — no como reemplazo general de la base relacional principal.

Ver también: [`../knowledge/aws-database/rds.md`](../knowledge/aws-database/rds.md), [`../knowledge/aws-database/dynamodb.md`](../knowledge/aws-database/dynamodb.md), [`repository-vs-jdbc.md`](./repository-vs-jdbc.md).

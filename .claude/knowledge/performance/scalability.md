# Scalability

> Ver [`../aws-compute/ec2.md`](../aws-compute/ec2.md) (Auto Scaling Group) y [`../devops/orchestration.md`](../devops/orchestration.md) (HorizontalPodAutoscaler) para los mecanismos técnicos de escalado ya documentados — este documento cubre el **criterio de diseño** que determina si un sistema puede escalar de esa forma en absoluto.

## Escalabilidad no es lo mismo que rendimiento

Un sistema puede ser rápido (baja latencia) con una única instancia y aun así no ser escalable (no mejora, o incluso empeora, al añadir más instancias) — escalabilidad es la propiedad de que el rendimiento agregado del sistema crece proporcionalmente al añadir capacidad, no una medida de velocidad individual.

## Horizontal vs. Vertical: ver la comparativa dedicada

Ver [`../../decisions/horizontal-vs-vertical-scaling.md`](../../decisions/horizontal-vs-vertical-scaling.md) para el análisis completo de cuándo elegir cada uno — este documento asume esa decisión ya tomada y cubre qué debe cumplir el sistema para que el escalado horizontal (la opción más común en arquitecturas cloud-native del framework) funcione realmente.

## El requisito estructural: sin estado local, o el estado no escala

```
Antipatrón: sesión de usuario almacenada en memoria local de una instancia —
al añadir más instancias, una petición podría llegar a una instancia que no tiene esa sesión

Correcto: estado compartido externalizado (Redis/ElastiCache, ver ../aws-database/elasticache.md),
cualquier instancia puede atender cualquier petición sin depender de estado local previo
```

Un servicio con estado en memoria local (caché no compartida, sesión local, ver [`../aws-network/alb.md`](../aws-network/alb.md) sección sticky sessions) no escala horizontalmente sin comprometer consistencia o requerir afinidad de sesión — el requisito de "stateless" no es una preferencia estilística, es la condición estructural que hace posible el escalado horizontal sin comportamiento inconsistente entre instancias.

## Escalabilidad de la base de datos: el cuello de botella más común al escalar la aplicación

```
La aplicación escala horizontalmente sin problema (sin estado) — pero todas las instancias
comparten la MISMA base de datos, que tiene un límite de capacidad de escritura no trivial de escalar
```

Ver [`../../decisions/rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md) y [`../sqlserver/partitioning.md`](../sqlserver/partitioning.md) — escalar la capa de aplicación es relativamente sencillo (añadir instancias); escalar la capa de persistencia relacional tiene límites estructurales que requieren decisiones de diseño explícitas (Read Replicas, particionamiento, o un modelo de datos distinto) mucho antes de alcanzar ese límite, no reactivamente cuando ya es un problema.

## Escalabilidad lineal vs. sub-lineal: verificar, no asumir

```
Escalabilidad lineal ideal: 2x instancias → 2x throughput
Escalabilidad sub-lineal real (habitual): 2x instancias → 1.6x throughput
                                            (contención compartida: BD, caché, red)
```

Añadir capacidad no garantiza una mejora proporcional — algún recurso compartido (base de datos, un servicio downstream con su propio límite) casi siempre introduce un techo de escalabilidad sub-lineal; medir el throughput real a distintos niveles de capacidad (ver [`benchmarking.md`](./benchmarking.md)) en lugar de asumir escalado lineal indefinido.

## Checklist

- [ ] ¿El servicio es genuinamente stateless, o depende de estado local que impide escalado horizontal sin afinidad de sesión?
- [ ] ¿Se ha identificado el recurso compartido (típicamente la base de datos) que limitará la escalabilidad antes que la capa de aplicación?
- [ ] ¿Se ha medido la escalabilidad real (throughput a distintos niveles de capacidad), no asumida como lineal?

Ver también: [`../../decisions/horizontal-vs-vertical-scaling.md`](../../decisions/horizontal-vs-vertical-scaling.md), [`capacity-planning.md`](./capacity-planning.md), [`bottleneck-analysis.md`](./bottleneck-analysis.md).

# Horizontal vs. Vertical Scaling

## Contexto

Dos formas fundamentalmente distintas de añadir capacidad a un sistema — ver [`../knowledge/performance/scalability.md`](../knowledge/performance/scalability.md) para el requisito estructural (statelessness) que el escalado horizontal exige.

## Comparativa

| Criterio | Escalado Horizontal (más instancias) | Escalado Vertical (instancia más grande) |
|---|---|---|
| Límite técnico | Prácticamente ilimitado (añadir más instancias) | Limitado al mayor tamaño de instancia disponible |
| Tolerancia a fallo | Alta — el fallo de una instancia no derriba el sistema | Baja — la instancia más grande es un único punto de fallo |
| Complejidad de la aplicación | Requiere diseño stateless (ver [`../knowledge/performance/scalability.md`](../knowledge/performance/scalability.md)) | Ninguna — la aplicación no necesita cambios |
| Coste marginal por unidad de capacidad | Generalmente lineal o mejor (instancias pequeñas más eficientes en coste/capacidad) | Con frecuencia creciente — instancias muy grandes tienen peor relación coste/capacidad |
| Downtime al escalar | Ninguno (añadir instancias sin interrumpir el servicio) | Habitualmente requiere reinicio/migración |
| Apropiado para bases de datos relacionales tradicionales | Requiere Read Replicas/particionamiento (ver [`../knowledge/sqlserver/partitioning.md`](../knowledge/sqlserver/partitioning.md)), no trivial | Más directo — aumentar recursos de la instancia |

## Cuándo elegir escalado horizontal

- La capa de aplicación es (o puede diseñarse) stateless — el caso por defecto para servicios web/API modernos del framework.
- Se requiere alta disponibilidad — el fallo de una única instancia no debe derribar el sistema completo.
- El crecimiento esperado de demanda excede lo que una única instancia, por grande que sea, podría soportar.

## Cuándo elegir escalado vertical

- La capa afectada tiene estado difícil de distribuir (una base de datos relacional tradicional antes de necesitar Read Replicas/particionamiento — ver [`../knowledge/sqlserver/partitioning.md`](../knowledge/sqlserver/partitioning.md)).
- El sistema no justifica aún la complejidad de rediseño para statelessness — solución más simple y rápida para una necesidad de capacidad puntual y moderada.
- Software con licenciamiento por instancia donde múltiplicar instancias incrementa coste de licencia desproporcionadamente frente a una instancia más grande.

## Combinación habitual, no mutuamente excluyentes

```
Capa de aplicación: escalado horizontal (stateless, Auto Scaling Group/HPA)
Capa de base de datos: escalado vertical hasta cierto límite, luego Read Replicas
                        (escalado horizontal de lecturas) + particionamiento si es necesario
```

El patrón más común en sistemas reales no es elegir uno para todo el sistema, sino aplicar cada uno donde corresponde según las características de cada capa.

## Recomendación por defecto de este framework

**Escalado horizontal por defecto para la capa de aplicación** (alineado con el diseño stateless que el framework ya exige en Clean Architecture, ver [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md)); **escalado vertical como primer paso pragmático para la capa de datos**, evolucionando a Read Replicas/particionamiento cuando el límite vertical se aproxima, no antes de que sea necesario (ver [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).

Ver también: [`../knowledge/performance/scalability.md`](../knowledge/performance/scalability.md), [`rds-vs-dynamodb.md`](./rds-vs-dynamodb.md), [`ecs-vs-eks.md`](./ecs-vs-eks.md).

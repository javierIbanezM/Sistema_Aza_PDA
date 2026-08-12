# DynamoDB

> Ver [`../../decisions/rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md) para la comparativa completa con bases de datos relacionales.

## Qué es: base de datos NoSQL de clave-valor/documento, totalmente gestionada

Escalado automático prácticamente ilimitado, latencia de milisegundos de un dígito consistente, sin gestión de servidores — a cambio de un modelo de datos muy distinto al relacional: **diseñar la tabla según los patrones de acceso**, no según un modelo normalizado.

## Diseño por patrón de acceso, no por normalización

```
Antipatrón (pensamiento relacional): tabla Pedidos + tabla LineasPedido + JOIN en la aplicación
DynamoDB idiomático: modelar la Partition Key/Sort Key según CÓMO se va a consultar,
                     a menudo denormalizando o usando "single table design"
```

A diferencia de un modelo relacional donde se normaliza primero y se optimiza después (ver [`../architecture/ddd.md`](../architecture/ddd.md) para el enfoque relacional del framework), en DynamoDB el patrón de acceso **debe** definirse antes de diseñar el esquema — cambiar el patrón de acceso después de diseñar la tabla suele requerir rediseñar el esquema completo, no solo añadir un índice.

## Partition Key y Sort Key: la base del rendimiento

```
Partition Key: clienteId    Sort Key: pedidoId
→ Permite: obtener todos los pedidos de un cliente ordenados, eficientemente
→ NO permite eficientemente: buscar pedidos por estado sin escanear (requiere un GSI, ver abajo)
```

Una Partition Key mal elegida (baja cardinalidad, patrón de acceso muy desigual entre particiones — *hot partition*) degrada el rendimiento de forma severa, ya que DynamoDB reparte capacidad por partición física.

## Global Secondary Index (GSI): consultas adicionales sin escanear

```
GSI: EstadoPedido (partition) + FechaCreacion (sort)
→ Permite: consultar pedidos por estado sin escanear la tabla completa
```

Cada patrón de consulta adicional necesario más allá de la clave primaria requiere, típicamente, un GSI diseñado específicamente para él — a diferencia de SQL donde un índice adicional se añade con relativa flexibilidad post-hoc (ver [`../sqlserver/indexes.md`](../sqlserver/indexes.md)), en DynamoDB el conjunto de patrones de consulta soportados debe anticiparse en gran medida durante el diseño.

## Capacidad: On-Demand vs. Provisioned

| Modo | Cuándo |
|---|---|
| On-Demand | Tráfico impredecible/variable, prioriza simplicidad sobre coste óptimo |
| Provisioned + Auto Scaling | Tráfico predecible, coste más eficiente a volumen alto y estable |

## Consistencia: eventual por defecto, fuerte opcional con coste

```
GetItem con ConsistentRead=true → lectura fuertemente consistente, mayor coste/latencia
GetItem sin ese parámetro → lectura eventualmente consistente, más barata/rápida
```

Ver [`../../patterns/cqrs.md`](../../patterns/cqrs.md) — el mismo principio de consistencia eventual del framework aplica; elegir consistencia fuerte solo cuando el caso de uso genuinamente lo requiere, no por defecto.

## Checklist

- [ ] ¿El diseño de la tabla parte de los patrones de acceso reales, no de un modelo normalizado adaptado después?
- [ ] ¿La Partition Key tiene suficiente cardinalidad para evitar hot partitions?
- [ ] ¿Cada patrón de consulta necesario está cubierto por la clave primaria o un GSI diseñado explícitamente?
- [ ] ¿Se ha evaluado si la consistencia eventual por defecto es aceptable para el caso de uso, antes de forzar consistencia fuerte con su coste adicional?

Ver también: [`rds.md`](./rds.md), [`../../decisions/rds-vs-dynamodb.md`](../../decisions/rds-vs-dynamodb.md), [`../../patterns/cqrs.md`](../../patterns/cqrs.md).

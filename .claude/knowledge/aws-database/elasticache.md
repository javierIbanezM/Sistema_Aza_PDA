# ElastiCache

> Ver `CLAUDE.md` sección 6.5 (Redis) y [`../spring/spring-cache.md`](../spring/spring-cache.md) para el patrón de caché general del framework — ElastiCache es la versión gestionada de Redis/Memcached en AWS.

## Redis vs. Memcached: cuál elegir

| | Redis | Memcached |
|---|---|---|
| Estructuras de datos | Ricas (hash, sorted set, listas, streams) | Solo clave-valor simple |
| Persistencia | Opcional (snapshot/AOF) | Ninguna — puramente volátil |
| Replicación/alta disponibilidad | Sí, nativa | No nativa |
| Multi-hilo | No (single-threaded por naturaleza, aunque clusters distribuyen carga) | Sí, multi-hilo nativo |

Redis como opción por defecto salvo un caso muy específico de caché puramente efímera y simple donde el multi-hilo de Memcached aporta ventaja medida — la mayoría de casos de uso del framework (caché de sesión, rate limiting, colas ligeras) se benefician de las estructuras de datos ricas de Redis.

## Modo Cluster: escalado horizontal

```
Cluster con 3 shards, cada shard con 1 primario + 2 réplicas
→ Escala capacidad de escritura repartiendo claves entre shards (sharding automático por hash de clave)
```

Sin modo Cluster, Redis está limitado a la capacidad de un único nodo primario para escrituras — el modo Cluster reparte el espacio de claves entre múltiples shards, permitiendo escalar horizontalmente más allá de la capacidad de una única instancia.

## Multi-AZ con failover automático

Igual principio que RDS Multi-AZ (ver [`rds.md`](./rds.md)) — una réplica en otra zona de disponibilidad permite failover automático ante fallo del nodo primario, sin intervención manual, minimizando la ventana de indisponibilidad de la caché.

## TTL: obligatorio, mismo principio que en cualquier caché

```
EXPIRE clave 3600  # nunca una clave sin TTL en un uso de caché
```

Ver `CLAUDE.md` sección 6.5 — toda clave de caché debe tener TTL explícito; ElastiCache no cambia este principio, solo gestiona la infraestructura subyacente.

## Casos de uso más allá de caché de aplicación

- **Rate limiting**: contadores con TTL por ventana de tiempo (ver `CLAUDE.md` sección 12.1, mitigación de fuerza bruta).
- **Sesiones distribuidas**: estado de sesión compartido entre múltiples instancias de la aplicación, en lugar de sesión en memoria local de cada instancia.
- **Pub/Sub ligero**: Redis Streams o Pub/Sub nativo para mensajería de baja latencia sin la complejidad de una cola completa (ver [`../aws-integration/sqs.md`](../aws-integration/sqs.md) para cuándo sí se necesita esa complejidad).

## Checklist

- [ ] ¿Se ha elegido Redis sobre Memcached salvo justificación específica de multi-hilo?
- [ ] ¿El modo Cluster está habilitado si la capacidad de un único nodo es insuficiente?
- [ ] ¿Multi-AZ está habilitado para cachés críticas en producción?
- [ ] ¿Toda clave almacenada tiene TTL explícito?

Ver también: `CLAUDE.md` sección 6.5, [`../spring/spring-cache.md`](../spring/spring-cache.md), [`rds.md`](./rds.md).

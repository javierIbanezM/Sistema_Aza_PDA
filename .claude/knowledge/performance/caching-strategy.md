# Caching Strategy

> Ver [`../aws-database/elasticache.md`](../aws-database/elasticache.md) para la implementación gestionada de Redis/Memcached, y [`../spring/spring-cache.md`](../spring/spring-cache.md) para la anotación `@Cacheable` de Spring — este documento cubre la **estrategia** de cuándo, qué y cómo cachear, transversal a cualquier tecnología de implementación.

## Cachear resuelve el síntoma, no siempre la causa

Añadir una caché ante una query lenta puede ocultar temporalmente el problema (la query sigue siendo lenta, solo se ejecuta con menos frecuencia) sin resolver la causa raíz — evaluar primero si el problema real tiene una solución estructural (un índice faltante, ver [`../sqlserver/indexes.md`](../sqlserver/indexes.md)) antes de añadir una capa de caché que añade su propia complejidad (invalidación, consistencia).

## Patrones de caché: cada uno con su propio trade-off

| Patrón | Comportamiento | Trade-off |
|---|---|---|
| Cache-aside (lazy loading) | La aplicación consulta la caché; si falla, consulta el origen y actualiza la caché | Simple, pero la primera consulta tras expirar siempre es lenta (cache miss) |
| Write-through | Cada escritura actualiza caché y origen simultáneamente | Caché siempre consistente, pero cada escritura es más lenta (dos operaciones) |
| Write-behind (write-back) | La escritura va primero a caché, se persiste al origen de forma asíncrona | Escrituras rápidas, pero riesgo de pérdida de datos si la caché falla antes de persistir |
| Read-through | La propia caché consulta el origen automáticamente ante un miss | Transparente para la aplicación, requiere que la caché soporte este modo nativamente |

Cache-aside es el patrón por defecto más común (ver [`../../recipes/android-create-room-database.md`](../../recipes/android-create-room-database.md) para un ejemplo de este mismo patrón aplicado a caché local en Android) — los demás se eligen ante requisitos específicos de consistencia o latencia de escritura.

## Invalidación: el problema genuinamente difícil de cachear

```
TTL fijo: simple, pero la caché puede servir datos desactualizados hasta que expire
Invalidación activa: al modificar el dato de origen, invalidar explícitamente la entrada de caché
                      correspondiente — más preciso, pero requiere identificar TODAS las
                      operaciones que deberían invalidar cada entrada
```

"Hay solo dos cosas difíciles en computación: invalidación de caché y nombrar cosas" — la dificultad real es que un dato cacheado puede volverse incorrecto por una escritura que el código de invalidación no anticipó (una actualización desde un proceso batch, una modificación directa en base de datos); ningún patrón elimina completamente este riesgo, solo lo acota.

## Qué cachear: datos con alta relación lectura/escritura y tolerancia a staleness

```
Buen candidato: catálogo de productos, consultado miles de veces por cada actualización
Mal candidato: saldo de cuenta bancaria en tiempo real, donde staleness tiene coste de negocio alto
```

Evaluar explícitamente, para cada dato candidato a cachear: ¿cuántas veces se lee frente a cuántas se escribe? ¿qué coste tiene servir un valor desactualizado durante el TTL? Datos de alta lectura y baja tolerancia a cambio son los candidatos ideales.

## Caché en múltiples niveles

```
Nivel 1: caché local en memoria de la instancia (más rápida, pero no compartida entre instancias)
Nivel 2: caché distribuida (Redis/ElastiCache, compartida, algo más lenta que local)
Nivel 3: origen de datos (base de datos, la fuente de verdad)
```

Combinar niveles reduce la carga sobre la caché distribuida y sobre el origen — con el coste añadido de gestionar consistencia entre más capas; apropiado cuando el volumen de lecturas justifica la complejidad adicional.

## Checklist

- [ ] ¿Se ha evaluado si existe una solución estructural al problema antes de añadir una capa de caché?
- [ ] ¿El patrón de caché elegido corresponde al requisito real de consistencia (cache-aside vs. write-through/behind)?
- [ ] ¿La estrategia de invalidación cubre todos los caminos de escritura que podrían dejar la caché desactualizada?
- [ ] ¿El dato cacheado tiene alta relación lectura/escritura y tolerancia aceptable a staleness?

Ver también: [`../aws-database/elasticache.md`](../aws-database/elasticache.md), [`bottleneck-analysis.md`](./bottleneck-analysis.md), [`../../patterns/cqrs.md`](../../patterns/cqrs.md).

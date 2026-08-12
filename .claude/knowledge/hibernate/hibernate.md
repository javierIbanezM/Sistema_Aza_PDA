# Hibernate

> Ver [`jpa.md`](./jpa.md) primero para el mapeo estándar de la especificación JPA. Este documento cubre comportamientos y optimizaciones **específicas de Hibernate** como implementación concreta, más allá de lo que exige la especificación.

## El Persistence Context (sesión de primer nivel)

Hibernate mantiene una caché de primer nivel (Persistence Context / `EntityManager`) con alcance de transacción: dentro de la misma transacción, cargar la misma entidad por ID dos veces devuelve la **misma instancia** en memoria, sin ir dos veces a base de datos.

```java
@Transactional
public void ejemplo() {
    PedidoEntity p1 = entityManager.find(PedidoEntity.class, "p-1");
    PedidoEntity p2 = entityManager.find(PedidoEntity.class, "p-1");
    // p1 == p2 (misma instancia), solo 1 query ejecutada
}
```

Esto es transparente pero tiene una implicación importante: los cambios sobre una entidad gestionada (*managed*) se sincronizan automáticamente con la base de datos al final de la transacción (*dirty checking*), **sin necesidad de llamar a `save()` explícitamente**:

```java
@Transactional
public void confirmarPedido(String id) {
    PedidoEntity entity = jpaRepository.findById(id).orElseThrow();
    entity.setEstado(EstadoPedidoEntity.CONFIRMADO); // no hace falta save(): Hibernate detecta el cambio
} // UPDATE automático al hacer commit de la transacción
```

## `@Transactional`: límites y propagación

- `@Transactional` en la capa de `@Service` (casos de uso), nunca en `@Repository` ni en el `@RestController`.
- Propagación por defecto (`REQUIRED`): si ya existe una transacción activa, la reutiliza; si no, crea una nueva.
- `readOnly = true` en operaciones de solo lectura — Hibernate puede optimizar (evita dirty checking innecesario) y comunica la intención explícitamente:

```java
@Transactional(readOnly = true)
public PedidoResponse buscarPedido(String id) { /* ... */ }

@Transactional // lectura-escritura, valor por defecto
public void confirmarPedido(String id) { /* ... */ }
```

- `LazyInitializationException` ocurre cuando se accede a una relación `LAZY` **fuera** del alcance de la transacción/sesión (típicamente, tras devolver la entidad desde el `@Service` hacia el `@RestController` y acceder a una relación no cargada). Se evita cargando explícitamente lo necesario **dentro** de la transacción (`JOIN FETCH`) y mapeando a DTO antes de salir de ella — nunca extendiendo el alcance de la sesión artificialmente (patrón "Open Session In View", desaconsejado por acoplar la capa de persistencia al ciclo de vida de la petición HTTP).

## Batch de inserts/updates

Por defecto, Hibernate ejecuta un `INSERT`/`UPDATE` por entidad, incluso dentro de la misma transacción. Para operaciones masivas, habilitar batching:

```yaml
spring:
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

Sin esto, insertar 10.000 registros ejecuta 10.000 roundtrips individuales a base de datos en lugar de agruparlos en lotes de 50 — impacto directo de rendimiento en cargas masivas (ver [`performance.md`](../../prompts/performance.md)).

## Segundo nivel de caché (opcional, con criterio)

El Persistence Context (primer nivel) tiene alcance de transacción. Hibernate soporta además una **caché de segundo nivel**, compartida entre transacciones (Ehcache, Caffeine, Redis como backend):

```java
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CatalogoProductoEntity { /* datos de referencia, poco cambiantes */ }
```

Aplicar **solo** a entidades de lectura frecuente y escritura poco frecuente (catálogos, configuración) — nunca a entidades transaccionales con escritura constante (`PedidoEntity`), donde la invalidación de caché sería más costosa que el beneficio. Ver [`../../standards/logging-standard.md`](../../standards/logging-standard.md) y sección 6.5 de `CLAUDE.md` para el patrón de caché general (Redis) como alternativa más explícita y controlable que el segundo nivel de Hibernate.

## `Session` vs. `EntityManager` vs. Spring Data Repository

| API | Nivel de abstracción |
|---|---|
| `Session` (Hibernate nativo) | Máximo control, específico de Hibernate, evitar salvo necesidad concreta no cubierta por JPA |
| `EntityManager` (JPA estándar) | Portable entre implementaciones JPA, usado directamente en queries complejas o `@Query` |
| `JpaRepository` (Spring Data) | Mayor nivel de abstracción, genera implementación automáticamente a partir de nombres de método/anotaciones |

Preferir `JpaRepository`/`@Query` (JPA estándar) sobre la API nativa de Hibernate (`Session`) salvo que se necesite explícitamente una característica no expuesta por JPA (ciertas optimizaciones de batch, `StatelessSession` para procesamiento masivo sin Persistence Context).

## `StatelessSession` para procesamiento masivo

Para migraciones de datos o jobs batch que procesan millones de filas, el Persistence Context de una sesión normal acumula todas las entidades en memoria (fuga de memoria potencial, ver [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md), y degradación de rendimiento). `StatelessSession` evita el Persistence Context y el dirty checking, a cambio de perder cascadas automáticas y el segundo nivel de caché — apropiado específicamente para este caso de uso, no como reemplazo general de `EntityManager`.

## Checklist

- [ ] ¿Las operaciones de solo lectura usan `@Transactional(readOnly = true)`?
- [ ] ¿Se ha verificado que no hay accesos a relaciones `LAZY` fuera del alcance de la transacción (`LazyInitializationException`)?
- [ ] ¿Las cargas masivas tienen `hibernate.jdbc.batch_size` configurado?
- [ ] ¿La caché de segundo nivel, si se usa, se aplica solo a datos de referencia poco cambiantes?
- [ ] ¿Los jobs de procesamiento masivo de datos evitan acumular el Persistence Context completo en memoria?

Ver también: [`jpa.md`](./jpa.md), [`performance.md`](../../prompts/performance.md), [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md), [`../../anti-patterns/memory-leak.md`](../../anti-patterns/memory-leak.md).

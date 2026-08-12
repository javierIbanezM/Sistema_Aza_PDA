# Knowledge — Hibernate

Índice de la documentación profunda de Hibernate/JPA como tecnología de persistencia, independiente de Spring (ver [`../spring/spring-data-jpa.md`](../spring/spring-data-jpa.md) para la capa de integración específica de Spring sobre esto).

| Documento | Descripción |
|---|---|
| [`jpa.md`](./jpa.md) | Mapeo estándar de la especificación JPA: entidades, fetch types, bloqueo optimista, migraciones |
| [`hibernate.md`](./hibernate.md) | Comportamientos específicos de Hibernate: Persistence Context, `@Transactional`, batch, segundo nivel de caché |

## Cómo usar este índice

- Para la integración con Spring Data (repositorios generados, query methods, Specifications) ver [`../spring/spring-data-jpa.md`](../spring/spring-data-jpa.md).
- Para el problema N+1 (muy relacionado con el fetch type de las relaciones) ver [`../../anti-patterns/n-plus-one.md`](../../anti-patterns/n-plus-one.md), global.
- Para Lombok en entidades JPA (qué anotaciones evitar) ver [`../lombok/lombok.md`](../lombok/lombok.md).

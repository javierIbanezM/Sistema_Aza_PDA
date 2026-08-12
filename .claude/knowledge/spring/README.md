# Knowledge — Spring

Índice de la documentación profunda de Spring / Spring Boot. Spring **no es Java** — vive en su propia carpeta, separado de [`../java/`](../java/) (lenguaje) y [`../hibernate/`](../hibernate/) (ORM), aunque los tres se usan juntos habitualmente. Los patrones de diseño y anti-patrones son globales, en [`../../patterns/`](../../patterns/) y [`../../anti-patterns/`](../../anti-patterns/).

| Documento | Descripción |
|---|---|
| [`spring-core.md`](./spring-core.md) | Contenedor Spring: inyección de dependencias, estereotipos, configuración, perfiles |
| [`spring-boot.md`](./spring-boot.md) | Auto-configuración, starters, arranque de aplicación, propiedades externalizadas |
| [`spring-security.md`](./spring-security.md) | Autenticación JWT, autorización granular, CORS, rate limiting |
| [`spring-data-jpa.md`](./spring-data-jpa.md) | Repositorios Spring Data, query methods, Specifications, paginación |
| [`spring-web.md`](./spring-web.md) | Controllers, manejo global de errores, interceptores |
| [`spring-validation.md`](./spring-validation.md) | Bean Validation, validadores personalizados, grupos de validación |
| [`spring-actuator.md`](./spring-actuator.md) | Health checks, métricas con Micrometer, seguridad de endpoints de gestión |
| [`spring-cache.md`](./spring-cache.md) | `@Cacheable`, TTL, coherencia entre réplicas |
| [`spring-events.md`](./spring-events.md) | `ApplicationEventPublisher`, eventos transaccionales, desacoplamiento de efectos secundarios |
| [`spring-aop.md`](./spring-aop.md) | Aspectos personalizados, proxies, límites de AOP |

## Cómo usar este índice

- Para persistencia (mapeo de entidades, comportamiento del ORM) ver [`../hibernate/`](../hibernate/) — `spring-data-jpa.md` aquí solo cubre la capa de integración de Spring, no el ORM en sí.
- Para el lenguaje Java en sí (records, streams, concurrencia) ver [`../java/`](../java/).
- Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí (regla de oro nº4).

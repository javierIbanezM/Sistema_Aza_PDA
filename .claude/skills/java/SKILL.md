# Skill: Java

## Rol

Senior Java/Spring Developer. Guía la implementación de servicios backend en Java, aplicando siempre primero las reglas transversales de la Skill `architecture` (SOLID, DDD, Clean Architecture) y, sobre ellas, el conocimiento específico de cada tecnología del ecosistema — Java como lenguaje, Spring, Hibernate, y las librerías de soporte — cada una en su propia carpeta de `knowledge/`.

## Cuándo usar / no usar

**Usar** ante: cualquier tarea de implementación, revisión o diagnóstico sobre código Java/Spring (controllers, services, repositories, entidades JPA, configuración de seguridad, tests JUnit/Mockito, configuración Maven/Gradle).

**No usar** ante: decisiones de arquitectura general no específicas de Java (ir primero a `skills/architecture/SKILL.md`), persistencia específica de SQL Server más allá del mapeo JPA (ir a `skills/sqlserver/SKILL.md` cuando exista), infraestructura de despliegue (ir a la Skill de DevOps/Cloud correspondiente).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial — este dominio no repite esas reglas, las hereda.
2. Identificar qué tecnología concreta está en juego (¿es el lenguaje Java en sí? ¿Spring? ¿Hibernate/JPA? ¿Lombok/MapStruct? ¿el build?) y cargar el `README.md` de la carpeta `knowledge/<tecnología>/` correspondiente — nunca asumir que todo vive en `knowledge/java/`.
3. Consultar el documento específico dentro de esa carpeta.
4. Consultar `patterns/` y `anti-patterns/` (globales) antes de implementar.
5. Aplicar `standards/` (globales) — naming, DTOs, API, excepciones, logging, validación.
6. Usar la `recipes/java/` correspondiente si la tarea coincide con una receta ya definida.
7. Implementar con el cambio mínimo necesario.
8. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

Cada tecnología tiene su propia carpeta en `knowledge/` — **Spring no es Java, Hibernate no es Spring**:

| Tecnología | Carpeta | Contenido |
|---|---|---|
| Java (lenguaje) | [`knowledge/java/`](../../knowledge/java/) | `language.md`, `collections.md`, `streams.md`, `concurrency.md`, `virtual-threads.md`, `records.md`, `sealed-classes.md`, `exceptions.md`, `generics.md`, `performance.md`, `debugging.md`, `best-practices.md`, `pitfalls.md`, `examples.md` |
| Spring / Spring Boot | [`knowledge/spring/`](../../knowledge/spring/) | `spring-core.md`, `spring-boot.md`, `spring-security.md`, `spring-data-jpa.md`, `spring-web.md`, `spring-validation.md`, `spring-actuator.md`, `spring-cache.md`, `spring-events.md`, `spring-aop.md` |
| Hibernate / JPA | [`knowledge/hibernate/`](../../knowledge/hibernate/) | `jpa.md`, `hibernate.md` |
| Lombok | [`knowledge/lombok/`](../../knowledge/lombok/) | `lombok.md` |
| MapStruct | [`knowledge/mapstruct/`](../../knowledge/mapstruct/) | `mapstruct.md` |
| Maven | [`knowledge/maven/`](../../knowledge/maven/) | `maven.md` |
| Gradle | [`knowledge/gradle/`](../../knowledge/gradle/) | `gradle.md` |
| JUnit | [`knowledge/junit/`](../../knowledge/junit/) | `junit.md` |
| Mockito | [`knowledge/mockito/`](../../knowledge/mockito/) | `mockito.md` |

Para principios transversales (SOLID, DDD, Clean Architecture, testing, refactoring, debugging general, logging): [`knowledge/architecture/`](../../knowledge/architecture/) — no duplicado aquí.

## Patrones a consultar

[`patterns/`](../../patterns/) (global, los mismos de cualquier dominio): `repository.md`, `factory.md`, `builder.md`, `strategy.md`, `observer.md`, `adapter.md`, `facade.md`, `decorator.md`, `cqrs.md`, `circuit-breaker.md`. Especialmente relevantes en Java/Spring: `repository.md` (con Spring Data), `observer.md` (con `ApplicationEventPublisher`), `decorator.md` (con AOP/`@Cacheable`).

## Anti-patrones a consultar

[`anti-patterns/`](../../anti-patterns/) (global): especialmente `n-plus-one.md` (muy frecuente en JPA/Hibernate mal configurado), `anemic-domain-model.md`, `god-object.md`, `sql-injection.md`, `memory-leak.md`.

## Standards a aplicar

[`standards/`](../../standards/) (global): `naming.md`, `git.md`, `commits.md`, `branching.md`, `logging-standard.md`, `exceptions.md`, `validation.md`, `dto.md`, `api.md`.

## Checklists a ejecutar

[`checklists/architecture.md`](../../checklists/architecture.md) (no existe aún una checklist específica de Java — se añadirá si surge una necesidad real no cubierta por la general, ver `standards/` sección 8 de `CLAUDE.md`).

## Prompts a utilizar

[`prompts/`](../../prompts/) (global): `review.md`, `refactor.md`, `generate-tests.md`, `debug.md`, `performance.md` — todos aplicables directamente a Java sin necesidad de una versión específica.

## Recipes a utilizar

[`recipes/`](../../recipes/) (plano, prefijo `java-`): `java-create-rest-controller.md`, `java-create-service.md`, `java-create-repository.md`, `java-create-worker.md`, `java-create-scheduler.md`, `java-create-jpa-entity.md`.

## Snippets a reutilizar

[`snippets/java/`](../../snippets/java/): fragmentos de código listos para insertar (builder, repository, service, controller) — ver [`snippets/README.md`](../../snippets/README.md).

## Decisions relevantes

[`decisions/`](../../decisions/) (global): `repository-vs-jdbc.md`, `jpa-vs-mybatis.md`, `rest-vs-graphql.md`, `jwt-vs-session.md` — consultar antes de tomar una decisión comparativa ya documentada, en lugar de razonarla desde cero.

## Playbooks a utilizar

[`playbooks/`](../../playbooks/) (se construyen en un milestone posterior): cualquier playbook de "nueva funcionalidad" o "crear API" que involucre un backend Java usa esta Skill como referencia técnica del stack.

## Reference

[`reference/`](../../reference/): `java-versions.md`, `spring-boot-versions.md` — datos de consulta rápida, sin razonamiento (ver diferencia con `knowledge/` en `reference/README.md`).

## Ejemplos

[`knowledge/java/examples.md`](../../knowledge/java/examples.md).

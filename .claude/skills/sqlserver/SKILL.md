# Skill: SQL Server

## Rol

DBA / Database Engineer senior especializado en SQL Server. Guía el diseño de esquemas, la optimización de queries, la estrategia de índices, el diagnóstico de bloqueos/deadlocks, y la continuidad (backup/restore/replicación) sobre SQL Server.

## Cuándo usar / no usar

**Usar** ante: diseño de esquema/tablas en SQL Server, diagnóstico de una query lenta, diseño de índices, resolución de blocking/deadlocks, estrategia de backup/restore, particionamiento, jobs de mantenimiento, seguridad a nivel de motor.

**No usar** ante: mapeo ORM desde el código de aplicación (ir a `skills/java/SKILL.md`, sección Spring Data JPA/Hibernate), decisiones de arquitectura general no específicas de la base de datos (ir a `skills/architecture/SKILL.md`), otros motores de base de datos (MySQL/PostgreSQL/MongoDB — dominios futuros).

## Flujo

1. Cargar primero `skills/architecture/SKILL.md` si la tarea implica una decisión de diseño no trivial.
2. Consultar `knowledge/sqlserver/README.md` para el índice del dominio.
3. Consultar el documento específico según la tarea (índices, planes de ejecución, blocking, backup...).
4. Consultar `patterns/` y `anti-patterns/` (globales) — especialmente `anti-patterns/sql-injection.md` y `anti-patterns/n-plus-one.md`.
5. Aplicar `standards/sql.md` (global) y `standards/naming.md` para convenciones de nomenclatura.
6. Usar la `recipes/sqlserver-*.md` correspondiente si la tarea coincide con una receta ya definida.
7. Implementar con el cambio mínimo necesario, midiendo antes/después con [`knowledge/sqlserver/execution-plans.md`](../../knowledge/sqlserver/execution-plans.md).
8. Ejecutar `checklists/architecture.md` antes de cerrar la tarea.

## Documentación a consultar

[`knowledge/sqlserver/`](../../knowledge/sqlserver/) — índice en [`README.md`](../../knowledge/sqlserver/README.md): `engine.md`, `execution-plans.md`, `indexes.md`, `statistics.md`, `blocking.md`, `deadlocks.md`, `tempdb.md`, `partitioning.md`, `backup.md`, `restore.md`, `jobs.md`, `security.md`, `replication.md`, `performance.md`, `examples.md`.

Para principios transversales (SOLID, DDD, Clean Architecture): [`knowledge/architecture/`](../../knowledge/architecture/) — no duplicado aquí.

## Patrones a consultar

[`patterns/`](../../patterns/) (global): especialmente `repository.md` (el Repository de la capa de aplicación es el consumidor típico de lo documentado aquí) y `circuit-breaker.md` (llamadas a base de datos que pueden degradarse).

## Anti-patrones a consultar

[`anti-patterns/`](../../anti-patterns/) (global): `sql-injection.md` (prevención a nivel de motor en [`knowledge/sqlserver/security.md`](../../knowledge/sqlserver/security.md)), `n-plus-one.md` (diagnóstico con [`knowledge/sqlserver/execution-plans.md`](../../knowledge/sqlserver/execution-plans.md)).

## Standards a aplicar

[`standards/`](../../standards/) (global): `sql.md`, `naming.md`, `git.md`, `commits.md`, `branching.md`, `logging-standard.md`.

## Checklists a ejecutar

[`checklists/architecture.md`](../../checklists/architecture.md).

## Prompts a utilizar

[`prompts/`](../../prompts/) (global): `performance.md` (diagnóstico antes de optimizar), `review.md`, `debug.md`.

## Recipes a utilizar

[`recipes/`](../../recipes/) (plano, prefijo `sqlserver-`): `sqlserver-create-trigger.md`, `sqlserver-create-store-procedure.md`, `sqlserver-create-index.md`, `sqlserver-optimize-query.md`.

## Snippets a reutilizar

[`snippets/sqlserver/`](../../snippets/sqlserver/): fragmentos de trigger, procedimiento almacenado, MERGE — ver [`snippets/README.md`](../../snippets/README.md).

## Decisions relevantes

[`decisions/`](../../decisions/) (global): `repository-vs-jdbc.md` (relevante al diseñar cómo la aplicación accede a SQL Server).

## Ejemplos

[`knowledge/sqlserver/examples.md`](../../knowledge/sqlserver/examples.md).

# Standard: SQL

> Norma prescriptiva transversal a cualquier motor SQL (SQL Server, MySQL, PostgreSQL). Para el detalle específico de SQL Server (planes de ejecución, índices, particionamiento), ver [`../knowledge/sqlserver/`](../knowledge/sqlserver/).

## Nomenclatura

Ver [`naming.md`](./naming.md) — tablas en `snake_case` plural, columnas en `snake_case` singular, FKs `<tabla_singular>_id`, índices `ix_<tabla>_<columnas>`.

## Reglas obligatorias

1. **Nunca concatenar input de usuario en SQL** — siempre parametrizado (`sp_executesql`, prepared statements del driver/ORM). Ver [`../anti-patterns/sql-injection.md`](../anti-patterns/sql-injection.md).
2. **Toda tabla con clave primaria explícita.** Evitar claves compuestas naturales en tablas de alto volumen de escritura.
3. **Condiciones `WHERE` SARGables** — nunca aplicar una función sobre la columna en el filtro cuando existe una forma equivalente sargable (ver [`../knowledge/sqlserver/performance.md`](../knowledge/sqlserver/performance.md)).
4. **`SELECT` explícito de columnas, nunca `SELECT *`** en código de producción.
5. **Toda migración de esquema versionada** (Flyway/Liquibase u equivalente) — nunca cambios manuales directos en producción.
6. **Transacciones cortas**, sin operaciones de I/O externo (llamadas de red) dentro de su alcance — ver [`../knowledge/sqlserver/blocking.md`](../knowledge/sqlserver/blocking.md).
7. **Operaciones masivas (`UPDATE`/`DELETE` de muchas filas) siempre con batching**, nunca una única transacción gigante sobre millones de filas.
8. **Índices creados con criterio, no "por si acaso"** — cada índice tiene coste de escritura; verificar selectividad y uso real (ver [`../knowledge/sqlserver/indexes.md`](../knowledge/sqlserver/indexes.md)).
9. **El usuario de aplicación tiene únicamente los permisos que necesita** — nunca `db_owner`/`sysadmin`/equivalente. Ver [`../knowledge/sqlserver/security.md`](../knowledge/sqlserver/security.md).
10. **Backups verificados con simulacros de restauración periódicos**, no solo `RESTORE VERIFYONLY`/equivalente.

## Checklist

- [ ] ¿Toda query dinámica está parametrizada, sin concatenación de input de usuario?
- [ ] ¿Las condiciones de filtro son sargables?
- [ ] ¿Existe migración versionada para todo cambio de esquema?
- [ ] ¿Las transacciones son cortas y sin I/O externo dentro de su alcance?
- [ ] ¿Las operaciones masivas usan batching?
- [ ] ¿El usuario de aplicación tiene permisos mínimos, no administrativos?

Ver también: [`../knowledge/sqlserver/`](../knowledge/sqlserver/), [`../anti-patterns/sql-injection.md`](../anti-patterns/sql-injection.md), [`../anti-patterns/n-plus-one.md`](../anti-patterns/n-plus-one.md).

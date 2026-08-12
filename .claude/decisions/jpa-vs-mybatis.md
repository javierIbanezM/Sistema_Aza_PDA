# JPA/Hibernate vs. MyBatis

## Contexto

Ambos son frameworks de persistencia para Java, pero con filosofías opuestas: JPA/Hibernate es un ORM completo (mapea objetos a tablas automáticamente); MyBatis es un mapeador SQL (el desarrollador escribe el SQL, MyBatis mapea el resultado a objetos).

## Comparativa

| Criterio | JPA/Hibernate | MyBatis |
|---|---|---|
| Filosofía | ORM completo: el objeto es la unidad, el SQL es generado | SQL Mapper: el SQL es la unidad, el mapeo a objetos es automático sobre un SQL explícito |
| Control del SQL | Indirecto (JPQL, Criteria API) — ver [`../knowledge/hibernate/jpa.md`](../knowledge/hibernate/jpa.md) | Total y explícito — cada query se escribe en XML o anotaciones |
| Gestión de relaciones (JOIN, cascadas) | Automática vía anotaciones (`@OneToMany`, `@ManyToOne`) | Manual — el desarrollador escribe el JOIN y mapea el resultado |
| Caché de primer/segundo nivel | Incluida (Persistence Context, ver [`../knowledge/hibernate/hibernate.md`](../knowledge/hibernate/hibernate.md)) | No incluida por defecto (se puede añadir manualmente) |
| Curva de aprendizaje | Alta — requiere entender el ciclo de vida de las entidades | Media — básicamente SQL + mapeo, sin conceptos adicionales de ORM |
| Riesgo de N+1 oculto | Alto si no se configuran los fetch types correctamente (ver [`../anti-patterns/n-plus-one.md`](../anti-patterns/n-plus-one.md)) | Bajo — el SQL ejecutado es siempre el que se escribió explícitamente |
| Migraciones de esquema complejas/legacy | Puede ser rígido si el esquema no sigue convenciones ORM-friendly | Más flexible con esquemas legados o no normalizados |

## Cuándo elegir JPA/Hibernate

- Dominio modelado con DDD (ver [`../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md)), donde el modelo de objetos es el artefacto principal y el esquema de base de datos se deriva de él.
- Proyecto nuevo sin restricciones de esquema legado, con relaciones entre entidades relativamente estándar.
- Se prioriza velocidad de desarrollo en el CRUD del dominio principal.

## Cuándo elegir MyBatis

- Integración con un esquema de base de datos legado, complejo o que no sigue convenciones ORM-friendly (procedimientos almacenados extensos, vistas materializadas, esquemas heredados de sistemas antiguos).
- Equipo con fuerte preferencia/experiencia SQL que valora el control explícito por encima de la automatización.
- Reportes y consultas analíticas donde el SQL exacto importa más que el mapeo automático de relaciones.

## Recomendación por defecto de este framework

**JPA/Hibernate** para el dominio transaccional de aplicaciones nuevas (alineado con DDD y Clean Architecture, ver [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md)). Considerar MyBatis específicamente cuando el proyecto integra con un esquema de base de datos legado que no encaja bien con el modelo de mapeo automático de JPA, o cuando el equipo tiene experiencia consolidada con MyBatis y el proyecto no se beneficia claramente de las capacidades adicionales de un ORM completo.

Ver también: [`repository-vs-jdbc.md`](./repository-vs-jdbc.md), [`../knowledge/hibernate/`](../knowledge/hibernate/).

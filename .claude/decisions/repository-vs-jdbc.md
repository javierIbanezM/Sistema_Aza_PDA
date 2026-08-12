# Repository (+ ORM) vs. JDBC directo

## Contexto

Ambos enfoques resuelven el acceso a base de datos desde Java. La pregunta no es "¿cuál es mejor en abstracto?" sino "¿qué necesita este proyecto concreto?".

- **Repository + ORM** (JPA/Hibernate, ver [`../knowledge/hibernate/`](../knowledge/hibernate/)): mapea objetos de dominio a filas de base de datos automáticamente, oculta el SQL tras una API orientada a objetos.
- **JDBC directo** (o un micro-ORM como jOOQ/MyBatis en modo SQL explícito): el desarrollador escribe el SQL explícitamente y mapea el resultado a mano o con una librería ligera.

## Comparativa

| Criterio | Repository + ORM (JPA/Hibernate) | JDBC directo |
|---|---|---|
| Velocidad de desarrollo en CRUDs simples | Alta — mucho generado automáticamente | Media — cada query se escribe a mano |
| Control exacto del SQL ejecutado | Bajo — el ORM decide el SQL final, requiere entender su comportamiento (ver [`../anti-patterns/n-plus-one.md`](../anti-patterns/n-plus-one.md)) | Total — el SQL es exactamente el que se escribe |
| Curva de aprendizaje | Alta (Persistence Context, fetch types, dirty checking — ver [`../knowledge/hibernate/hibernate.md`](../knowledge/hibernate/hibernate.md)) | Baja — SQL estándar, sin capa intermedia que aprender |
| Rendimiento en queries complejas/reportes | Requiere cuidado (proyecciones, `@Query` nativo) | Óptimo por diseño — se escribe el SQL óptimo directamente |
| Portabilidad entre motores de BD | Alta (JPQL abstrae el dialecto SQL) | Baja — SQL específico del motor, salvo disciplina explícita |
| Riesgo de anti-patrones ocultos (N+1, LazyInitializationException) | Presente si no se domina el ORM | No aplica — no hay comportamiento "mágico" que dispare queries inesperadas |

## Cuándo elegir Repository + ORM

- Dominio con muchas entidades relacionadas y operaciones CRUD estándar predominantes.
- Equipo con experiencia en JPA/Hibernate, o disposición a invertir en aprenderlo bien (el coste de usarlo mal — ver [`../anti-patterns/n-plus-one.md`](../anti-patterns/n-plus-one.md) — es real).
- Se valora la velocidad de desarrollo y el mapeo automático por encima del control exhaustivo del SQL exacto.

## Cuándo elegir JDBC directo (o un micro-ORM tipo jOOQ)

- Reportes y consultas analíticas complejas, donde el SQL óptimo es difícil o imposible de expresar de forma natural en JPQL.
- Proyectos con requisitos de rendimiento muy estrictos donde el control exacto del SQL generado es crítico.
- Equipos con fuerte experiencia SQL que prefieren esa vía, o proyectos pequeños donde el overhead conceptual de un ORM completo no se justifica.

## Recomendación por defecto de este framework

**Repository + JPA/Hibernate** para el dominio transaccional principal (ver [`../knowledge/spring/spring-data-jpa.md`](../knowledge/spring/spring-data-jpa.md)), combinado con **queries nativas/`@Query` explícito** para reportes y consultas complejas puntuales dentro del mismo proyecto — no es una decisión excluyente, ambos enfoques conviven habitualmente en el mismo servicio según el tipo de operación.

Ver también: [`jpa-vs-mybatis.md`](./jpa-vs-mybatis.md), [`../patterns/repository.md`](../patterns/repository.md).

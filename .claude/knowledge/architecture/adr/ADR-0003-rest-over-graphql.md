# ADR-0003: REST como estilo de API por defecto (sobre GraphQL)

- **Estado**: Aceptado
- **Fecha**: 2026-07-30
- **Decisores**: Equipo de diseño de `claude-code-premium`

## Contexto

`standards/api.md` necesita fijar un único estilo de API por defecto para que los ejemplos, recipes y snippets de todos los dominios (Java/Spring, futuro Node/NestJS...) sean consistentes, sin tener que decidir REST vs. GraphQL en cada dominio de forma independiente.

## Decisión

El framework adopta **REST** como estilo de API por defecto (ver [`../../standards/api.md`](../../../standards/api.md)), reservando GraphQL para proyectos concretos con un caso de uso confirmado que lo justifique (ver comparativa completa en [`../../decisions/rest-vs-graphql.md`](../../../decisions/rest-vs-graphql.md)).

## Alternativas consideradas

Ver el análisis completo de criterios en [`../../decisions/rest-vs-graphql.md`](../../../decisions/rest-vs-graphql.md) — este ADR no repite la comparativa, solo documenta la decisión tomada para el framework en base a ella.

## Consecuencias

### Positivas
- Todas las recipes de creación de endpoints (`recipes/java-create-rest-controller.md`, futuras equivalentes en Node/Android) siguen el mismo estilo y las mismas convenciones de `standards/api.md`.
- Menor curva de entrada para cualquier desarrollador que use el framework — REST es el estándar más ampliamente conocido.

### Negativas / Trade-offs aceptados
- Proyectos con necesidades reales de composición flexible de datos por el cliente (múltiples apps con requisitos muy divergentes sobre el mismo dominio) no tienen una recipe de GraphQL lista — deberán documentar su propio ADR de excepción citando [`../../decisions/rest-vs-graphql.md`](../../../decisions/rest-vs-graphql.md), en lugar de partir de una recipe ya construida en este framework.

## Referencias

- [`../../decisions/rest-vs-graphql.md`](../../../decisions/rest-vs-graphql.md)
- [`../../standards/api.md`](../../../standards/api.md)

# REST vs. GraphQL

## Contexto

Ambos son estilos de diseño de API. Ver `CLAUDE.md` sección 15 y [`../standards/api.md`](../standards/api.md) para las convenciones de REST de este framework, y [`../knowledge/architecture/README.md`](../knowledge/architecture/README.md) (sección GraphQL) para los conceptos de GraphQL.

## Comparativa

| Criterio | REST | GraphQL |
|---|---|---|
| Forma de la respuesta | Fija por endpoint (el servidor decide qué campos devuelve) | El cliente decide exactamente qué campos necesita en cada petición |
| Over-fetching / under-fetching | Frecuente — un endpoint devuelve más o menos de lo que una vista concreta necesita | Resuelto por diseño — cada consulta pide exactamente lo necesario |
| Número de llamadas para una vista compuesta | Puede requerir varias llamadas a distintos endpoints | Una única query puede combinar datos de múltiples "recursos" |
| Cacheable con HTTP estándar (`ETag`, `Cache-Control`, CDN) | Sí, de forma nativa | Limitado — la mayoría de queries van por `POST`, requiere estrategias de caché específicas de GraphQL |
| Curva de aprendizaje / tooling | Baja, ubicua, cualquier cliente HTTP sirve | Media/alta — requiere cliente GraphQL, esquema, resolvers |
| Versionado | Explícito en la URL (`/v1/`, ver [`../standards/api.md`](../standards/api.md)) | Implícito — el esquema evoluciona de forma aditiva, sin versión en la URL |
| Riesgo de abuso (queries costosas) | Acotado por diseño (un endpoint = una operación conocida) | Requiere control de profundidad/complejidad de query (ver `CLAUDE.md` sección 15.2) |
| Problema N+1 en resolución de campos anidados | No aplica de la misma forma | Frecuente si no se usa `DataLoader` (ver `CLAUDE.md` sección 15.2 y [`../anti-patterns/n-plus-one.md`](../anti-patterns/n-plus-one.md)) |

## Cuándo elegir REST

- API pública consumida por muchos clientes distintos y desconocidos, donde la simplicidad y cacheabilidad HTTP estándar son prioritarias.
- Dominio con operaciones bien definidas que no varían mucho de forma de consumo entre clientes.
- Equipo/ecosistema sin experiencia previa en GraphQL y sin un caso de uso claro que lo justifique (evita complejidad innecesaria, ver [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).

## Cuándo elegir GraphQL

- Múltiples clientes con necesidades de datos muy distintas sobre el mismo dominio (ej. app móvil que necesita poco por pantalla vs. dashboard web que necesita mucho) — evita mantener múltiples endpoints REST especializados por cliente.
- Backend-for-Frontend (BFF) que agrega datos de varios servicios internos y necesita flexibilidad de composición por el cliente.
- El equipo tiene experiencia con GraphQL y el proyecto tiene relaciones de datos complejas y consultadas de formas muy variables.

## Recomendación por defecto de este framework

**REST** como opción por defecto (ver [`../standards/api.md`](../standards/api.md)) salvo que exista un caso de uso concreto y confirmado (múltiples clientes con necesidades de datos muy divergentes) que justifique el coste adicional de GraphQL — evaluar bajo el mismo criterio de YAGNI que cualquier otra decisión de arquitectura (ver [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).

Ver también: `CLAUDE.md` sección 15.

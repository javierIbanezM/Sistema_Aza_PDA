# Reference — Índice

Hojas de **consulta rápida**, no conocimiento explicativo. Aquí no se razona ni se justifica nada (eso vive en `knowledge/`) — solo se listan datos de referencia: versiones soportadas, tablas de tipos, comandos, mapeos rápidos.

Diferencia con `knowledge/` y `standards/`:

| Carpeta | Contenido | Ejemplo |
|---|---|---|
| `knowledge/` | Explica y razona (por qué, cuándo, cómo) | "Por qué usar Value Objects en DDD" |
| `standards/` | Prescribe una norma a seguir | "Los DTOs se nombran `<Accion><Entidad>Request`" |
| `reference/` | Datos de consulta rápida, sin razonamiento | "Tipos de datos de SQL Server y su equivalente en Java" |

## Contenido

- [`java-versions.md`](./java-versions.md) — versiones LTS de Java soportadas, novedades por versión
- [`spring-boot-versions.md`](./spring-boot-versions.md) — matriz de compatibilidad Spring Boot / Java
- [`sqlserver-types.md`](./sqlserver-types.md) — tipos de datos SQL Server y su equivalencia en Java
- [`node-versions.md`](./node-versions.md) — versiones LTS de Node.js soportadas y compatibilidad con frameworks

## Contenido previsto (se puebla por dominio a medida que se construyen los milestones)

- `android-sdk.md` — niveles de API de Android y compatibilidad
- `mysql-types.md` — tipos de datos MySQL y equivalencias
- `docker-commands.md` — comandos Docker de uso frecuente
- `git-commands.md` — comandos Git de uso frecuente

Cada dominio (`java`, `node`, `android`, `sqlserver`, `aws`...) añade aquí sus propias hojas de referencia según se vaya construyendo en los milestones correspondientes (ver roadmap en el `README.md` raíz del proyecto).

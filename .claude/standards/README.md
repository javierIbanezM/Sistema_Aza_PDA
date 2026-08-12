# Standards — Índice

Normas prescriptivas, transversales a todos los dominios tecnológicos del framework. A diferencia de `knowledge/`, que **explica y razona** (por qué, cuándo, ejemplos), `standards/` **prescribe**: la regla concreta a aplicar, con enlaces a `knowledge/` para quien necesite el razonamiento completo detrás de cada norma.

| Documento | Norma sobre |
|---|---|
| [`naming.md`](./naming.md) | Nomenclatura de clases, métodos, variables, paquetes, tablas, endpoints |
| [`git.md`](./git.md) | Uso general de Git: rebase/merge, conflictos, archivos grandes |
| [`commits.md`](./commits.md) | Formato de mensajes de commit (Conventional Commits) |
| [`branching.md`](./branching.md) | Nomenclatura y ciclo de vida de ramas |
| [`logging-standard.md`](./logging-standard.md) | Reglas obligatorias de logging (qué, cómo, qué nunca loguear) |
| [`exceptions.md`](./exceptions.md) | Jerarquía y manejo de excepciones |
| [`validation.md`](./validation.md) | Validación de datos de entrada, dos capas (borde + dominio) |
| [`dto.md`](./dto.md) | Diseño de DTOs, mapeo entre capas |
| [`api.md`](./api.md) | Convenciones REST, códigos HTTP, formato de error, paginación |
| [`sql.md`](./sql.md) | Normas SQL transversales a cualquier motor (parametrización, sargabilidad, transacciones cortas) |

Normas específicas de un dominio (`android.md`...) se añadirán aquí mismo, de forma global, a medida que se construyan los dominios correspondientes (ver roadmap en `README.md` raíz del proyecto) — nunca duplicadas dentro de `knowledge/<dominio>/`.

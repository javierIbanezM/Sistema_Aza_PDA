# Reference: Node.js Versions

Consulta rápida de versiones LTS. Sin razonamiento — ver [`../knowledge/node/`](../knowledge/node/) para explicaciones.

| Versión | Tipo | Fin de soporte | Características clave |
|---|---|---|---|
| 18 | LTS (Maintenance) | Abril 2025 | Fetch API nativa, Test runner nativo |
| 20 | LTS | Abril 2026 | Permission model experimental, `Array.prototype.with` |
| 22 | LTS | Abril 2027 | `require()` de ESM, `structuredClone` estable |
| 24 | LTS | Abril 2028 (estimado) | Verificar release notes oficiales para novedades exactas |

**Recomendación por defecto de este framework**: la LTS activa más reciente en el momento de iniciar el proyecto — nunca una versión "Current" (no-LTS) en producción.

## Compatibilidad rápida con frameworks

| Framework | Node mínimo típico |
|---|---|
| NestJS 10.x | 18 |
| Express 5.x | 18 |
| Fastify 5.x | 20 |

Verificar siempre la matriz de compatibilidad oficial de cada framework antes de fijar versiones en un proyecto nuevo.

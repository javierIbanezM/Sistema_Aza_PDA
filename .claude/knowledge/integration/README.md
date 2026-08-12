# Knowledge — Integration

Índice del dominio de **integración entre sistemas**: APIs propias consumidas externamente, ERPs, WMS, CRMs, colas de mensajería y servicios de terceros. Transversal a Java, Node y SQL Server — no específico de ningún lenguaje.

| Documento | Descripción |
|---|---|
| [`integration-patterns.md`](./integration-patterns.md) | Visión general: estilos de integración, catálogo de patrones, cuándo NO se necesitan |
| [`api-design.md`](./api-design.md) | Diseño de contrato para consumidores externos menos flexibles que un frontend propio |
| [`api-versioning.md`](./api-versioning.md) | Compatibilidad hacia atrás, estrategias de versionado, deprecación |
| [`idempotency.md`](./idempotency.md) | Idempotency Key, idempotencia natural vs. forzada, en consumidores de eventos |
| [`retries-and-backoff.md`](./retries-and-backoff.md) | Qué reintentar, backoff exponencial con jitter, límites |
| [`circuit-breaker.md`](./circuit-breaker.md) | Aplicación del patrón global en contexto de múltiples sistemas externos |
| [`saga.md`](./saga.md) | Coreografiada vs. orquestada, compensaciones |
| [`event-driven.md`](./event-driven.md) | Eventos entre sistemas distintos, notificación fina vs. estado completo |
| [`message-queues.md`](./message-queues.md) | Cuándo colas, DLQ, orden de mensajes, Schema Registry |
| [`schema-mapping.md`](./schema-mapping.md) | Anti-Corruption Layer, mapeo explícito de campos y enumerados |
| [`data-synchronization.md`](./data-synchronization.md) | Fuente de verdad, estrategias de sincronización, resolución de conflictos |
| [`observability.md`](./observability.md) | Métricas, health checks y alertas específicas de integraciones |
| [`distributed-tracing.md`](./distributed-tracing.md) | Correlación a través de sistemas sin soporte nativo del estándar |
| [`error-handling.md`](./error-handling.md) | Clasificación de errores de integración, traducción, timeouts diferenciados |
| [`best-practices.md`](./best-practices.md) | Síntesis de las reglas más importantes del dominio |
| [`pitfalls.md`](./pitfalls.md) | Errores frecuentes específicos de integración |
| [`examples.md`](./examples.md) | Casos de uso completos |

## Cómo usar este índice

- El patrón Circuit Breaker en sí vive en [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md) (global) — [`circuit-breaker.md`](./circuit-breaker.md) aquí solo cubre su aplicación específica a integración, sin duplicar el patrón.
- Para trazas/logging general, ver [`../architecture/logging.md`](../architecture/logging.md) — este dominio solo añade las consideraciones específicas de sistemas externos.
- Antes de crear un documento nuevo, comprueba que el tema no esté ya cubierto aquí ni en otro dominio (regla de oro nº2).

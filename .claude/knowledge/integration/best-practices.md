# Best Practices (Integration)

> Síntesis de las reglas más importantes de integración entre sistemas. Ver cada documento enlazado para el razonamiento completo.

## Diseño de contrato

- Diseñar para el consumidor menos flexible — sistemas externos (ERP/WMS) no se adaptan tan rápido como un frontend propio (ver [`api-design.md`](./api-design.md)).
- Compatibilidad hacia atrás por defecto; versión nueva solo ante cambios que rompen (ver [`api-versioning.md`](./api-versioning.md)).
- Todo evento publicado hacia otros sistemas es un contrato público, versionado igual que una API (ver [`event-driven.md`](./event-driven.md)).

## Resiliencia

- Toda operación de creación/modificación costosa soporta `Idempotency-Key` (ver [`idempotency.md`](./idempotency.md)).
- Reintentos solo ante errores transitorios, con backoff exponencial + jitter, nunca ante 4xx deterministas (ver [`retries-and-backoff.md`](./retries-and-backoff.md)).
- Circuit Breaker por sistema externo, nunca compartido, con umbrales calibrados individualmente (ver [`circuit-breaker.md`](./circuit-breaker.md)).
- Cada paso de una Saga con efecto real tiene su compensación diseñada desde el principio (ver [`saga.md`](./saga.md)).

## Datos

- Anti-Corruption Layer explícito en cualquier integración con un sistema externo (ver [`schema-mapping.md`](./schema-mapping.md)).
- Fuente de verdad definida explícitamente por dato sincronizado, nunca ambigua (ver [`data-synchronization.md`](./data-synchronization.md)).

## Observabilidad

- Métricas de latencia/error por sistema externo individual, health checks que verifican dependencias reales (ver [`observability.md`](./observability.md)).
- Correlación de traza preservada incluso a través de sistemas sin soporte nativo del estándar (ver [`distributed-tracing.md`](./distributed-tracing.md)).

## Errores

- Errores del sistema externo traducidos a excepciones de dominio propio, nunca propagados crudos (ver [`error-handling.md`](./error-handling.md)).

Ver [`pitfalls.md`](./pitfalls.md) y [`examples.md`](./examples.md).

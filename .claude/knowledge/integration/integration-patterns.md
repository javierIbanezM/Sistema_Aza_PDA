# Integration Patterns

> Visión general de los patrones de integración entre sistemas — cada uno desarrollado en su propio documento de esta carpeta o, si ya es un patrón de diseño general del framework, enlazado desde [`../../patterns/`](../../patterns/) sin duplicarse.

## El problema central de la integración

Integrar sistemas (APIs propias, ERPs, WMS, CRMs, servicios de terceros) introduce un problema que no existe dentro de un único sistema: **cada llamada a través de la red puede fallar de formas que una llamada a método local nunca falla** — timeout, respuesta parcial, duplicación por reintento del cliente, orden de llegada no garantizado, el otro sistema caído temporalmente. Todo patrón de integración existe para dar una respuesta explícita y probada a alguna de estas formas de fallo, en lugar de asumir implícitamente que "la llamada funcionará".

## Estilos de integración: síncrono vs. asíncrono

| Estilo | Cuándo | Documento |
|---|---|---|
| Síncrono (REST/gRPC) | El llamador necesita el resultado inmediatamente para continuar | [`api-design.md`](./api-design.md) |
| Asíncrono vía eventos | El llamador no necesita esperar; otros sistemas reaccionan de forma desacoplada | [`event-driven.md`](./event-driven.md) |
| Asíncrono vía colas | Se necesita garantía de entrega/procesamiento incluso si el consumidor está caído temporalmente | [`message-queues.md`](./message-queues.md) |

Ver `CLAUDE.md` sección 17 (Microservicios) para el criterio general de cuándo usar cada estilo.

## Catálogo de patrones cubiertos en este dominio

| Patrón | Resuelve | Documento |
|---|---|---|
| Idempotencia | Que un reintento no duplique el efecto de una operación | [`idempotency.md`](./idempotency.md) |
| Reintentos con backoff | Errores transitorios de red/disponibilidad | [`retries-and-backoff.md`](./retries-and-backoff.md) |
| Circuit Breaker | Fallos repetidos y prolongados de un sistema externo | [`circuit-breaker.md`](./circuit-breaker.md) → [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md) |
| Saga | Consistencia a través de múltiples sistemas sin transacción distribuida | [`saga.md`](./saga.md) |
| Mapeo de esquemas | Traducir el modelo de un sistema al de otro sin acoplarlos | [`schema-mapping.md`](./schema-mapping.md) |
| Sincronización de datos | Mantener datos coherentes entre sistemas con distinta fuente de verdad | [`data-synchronization.md`](./data-synchronization.md) |

## Cuándo NO se necesita ninguno de estos patrones

Integrar dos sistemas dentro del mismo proceso, con una llamada de método local, no necesita nada de esto — estos patrones existen específicamente para atravesar un límite de red/proceso/sistema, donde el fallo parcial es una posibilidad real, no teórica. Aplicarlos a una llamada local (por ejemplo, envolver una llamada a otro módulo del mismo servicio en un Circuit Breaker) es sobre-ingeniería (ver [`../architecture/dry-kiss-yagni.md`](../architecture/dry-kiss-yagni.md)).

## Checklist

- [ ] ¿Se ha identificado si la integración es genuinamente cruzando un límite de red/sistema, o es una llamada local que no necesita estos patrones?
- [ ] ¿Se ha elegido el estilo (síncrono/eventos/colas) según si el llamador necesita el resultado inmediato?
- [ ] ¿Se ha revisado el catálogo completo antes de improvisar una solución ad-hoc a un problema ya resuelto por un patrón conocido?

Ver también: [`api-design.md`](./api-design.md), [`event-driven.md`](./event-driven.md), `CLAUDE.md` sección 16-17.

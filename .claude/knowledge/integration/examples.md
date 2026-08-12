# Examples (Integration)

## Ejemplo 1 — Sincronizar pedidos confirmados con un ERP externo

**Contexto**: cada pedido confirmado en el sistema propio debe reflejarse en el ERP (SAP), que es lento y ocasionalmente indisponible.

**Flujo aplicado**:

1. Se elige integración asíncrona vía cola (no llamada síncrona en el flujo de confirmación) — ver [`integration-patterns.md`](./integration-patterns.md) y [`message-queues.md`](./message-queues.md), ya que el usuario no debe esperar a que el ERP responda para confirmar su pedido.
2. El evento `PedidoConfirmadoEvent` se publica tras el commit de la transacción local (ver [`../spring/spring-events.md`](../spring/spring-events.md), `AFTER_COMMIT`).
3. Un consumidor dedicado traduce el evento al formato del ERP vía un Anti-Corruption Layer (`SapPedidoAdapter`, ver [`schema-mapping.md`](./schema-mapping.md)).
4. La llamada al ERP está protegida por un Circuit Breaker específico (`cb-erp-sap`, ver [`circuit-breaker.md`](./circuit-breaker.md)) con reintentos de backoff exponencial (ver [`retries-and-backoff.md`](./retries-and-backoff.md)) para fallos transitorios.
5. Ante fallo persistente (circuito abierto), el mensaje va a una Dead Letter Queue con alerta al equipo (ver [`message-queues.md`](./message-queues.md)).
6. El consumidor verifica idempotencia por `pedidoId + version` antes de reenviar al ERP, tolerando reentrega del broker (ver [`idempotency.md`](./idempotency.md)).
7. Métricas de latencia/tasa de error específicas de esta integración, visibles en el dashboard consolidado (ver [`observability.md`](./observability.md)).

---

## Ejemplo 2 — Confirmar un pedido con Saga orquestada (WMS + Pagos + ERP)

**Contexto**: confirmar un pedido requiere reservar stock (WMS), cobrar (proveedor de pagos) y registrar en el ERP — tres sistemas, sin transacción distribuida posible.

**Flujo aplicado**: ver el ejemplo completo de código en [`saga.md`](./saga.md) — Saga orquestada elegida por la complejidad de compensaciones (liberar stock, reembolsar) y el número de sistemas heterogéneos involucrados. Cada paso tiene su compensación diseñada desde el principio, ejecutada en orden inverso ante fallo de un paso posterior.

---

## Ejemplo 3 — Diagnosticar una integración degradada silenciosamente

**Contexto**: soporte reporta que algunos pedidos tardan "a veces" en reflejarse en el WMS, sin error visible en los logs propios.

**Flujo aplicado** (siguiendo [`../../agents/senior-debugger.md`](../../agents/senior-debugger.md)):

1. Se revisa el dashboard de observabilidad de integraciones (ver [`observability.md`](./observability.md)) — se detecta que la latencia p95 hacia el WMS ha crecido gradualmente en las últimas semanas, sin llegar a disparar ninguna alerta configurada con umbral fijo.
2. Se confirma con trazas distribuidas (ver [`distributed-tracing.md`](./distributed-tracing.md)) que el tiempo se consume específicamente en la llamada al WMS, no en procesamiento propio.
3. Se ajustan las alertas para basarse en un SLO explícito en lugar del umbral fijo que no había disparado (ver [`observability.md`](./observability.md)).
4. Se evalúa con el equipo del WMS si la degradación es conocida de su lado; mientras tanto, se ajustan los timeouts y el umbral del Circuit Breaker según el nuevo comportamiento observado (ver [`circuit-breaker.md`](./circuit-breaker.md)).

---

## Ejemplo 4 — Diseñar el contrato de un nuevo webhook para un WMS externo

**Contexto**: el WMS necesita poder notificar al sistema propio cuando un envío se marca como completado.

**Flujo aplicado**, siguiendo [`../../playbooks/new-api.md`](../../playbooks/new-api.md):

1. Se diseña el endpoint receptor del webhook con `standards/api.md` — incluida verificación de firma/autenticación del webhook (el WMS debe poder demostrar que la notificación es legítima).
2. Se documenta el contrato exacto que el WMS debe enviar (ver [`api-design.md`](./api-design.md)), incluida una `Idempotency-Key` esperada del lado del WMS, ya que los webhooks pueden reentregarse ante fallo de confirmación.
3. El endpoint verifica idempotencia antes de procesar (ver [`idempotency.md`](./idempotency.md)) y traduce el payload del WMS al modelo propio vía Anti-Corruption Layer (ver [`schema-mapping.md`](./schema-mapping.md)).
4. Se valida con el equipo del WMS que el contrato definido es compatible con lo que su sistema puede enviar realmente, antes de considerarlo estable.

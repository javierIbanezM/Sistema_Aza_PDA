# Circuit Breaker (en integración de sistemas)

> El patrón en sí está documentado de forma global en [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md) — este documento no lo repite, cubre **cómo aplicarlo específicamente en un contexto de integración con múltiples sistemas externos heterogéneos** (ERPs, WMS, CRMs), donde la disciplina de aplicación es distinta a la de un único servicio interno.

## Un Circuit Breaker por sistema externo, nunca compartido

```java
CircuitBreaker cbErp = CircuitBreaker.of("erp-sap", configErp);
CircuitBreaker cbWms = CircuitBreaker.of("wms-manhattan", configWms);
CircuitBreaker cbPagos = CircuitBreaker.of("proveedor-pagos", configPagos);
```

Ver [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md), sección "Errores comunes" — en un escenario de integración con varios sistemas externos, compartir un único Circuit Breaker mezclaría la fiabilidad de sistemas con perfiles completamente distintos (un ERP legado con SLA pobre junto a un proveedor de pagos moderno con alta disponibilidad), produciendo aperturas de circuito que no reflejan el estado real de ninguno de los dos individualmente.

## Umbrales calibrados por sistema, no un valor único genérico

Cada sistema externo tiene un perfil de fiabilidad distinto — un ERP legado con latencia históricamente alta y picos de indisponibilidad conocidos necesita umbrales más tolerantes que una API moderna con SLA de 99.9%; aplicar el mismo umbral de "50% de fallos abre el circuito" a ambos sin calibrar según el comportamiento real observado de cada uno produce falsos positivos (circuito abierto ante variabilidad normal) o falsos negativos (circuito que nunca se abre ante degradación real).

## Fallback específico por tipo de integración

```java
// Integración de solo lectura (consultar stock en el WMS): fallback con caché de la última lectura conocida
public StockDisponible consultarStock(String sku) {
    return circuitBreakerWms.executeSupplier(() -> wmsClient.consultarStock(sku))
        .recover(ex -> cacheStock.obtenerUltimoConocido(sku)); // dato potencialmente desactualizado, pero disponible
}

// Integración de escritura crítica (confirmar pedido en el ERP): fallback de cola diferida, nunca datos falsos
public void confirmarPedidoEnErp(Pedido pedido) {
    circuitBreakerErp.executeRunnable(() -> erpClient.confirmarPedido(pedido))
        .recover(ex -> colaReintentoDiferido.encolar(pedido)); // se procesará cuando el ERP se recupere
}
```

El fallback correcto depende del tipo de operación: para lecturas, un valor cacheado potencialmente desactualizado suele ser aceptable; para escrituras con efecto de negocio, el fallback debe preservar la operación para procesarla más tarde (ver [`message-queues.md`](./message-queues.md)), nunca simular un éxito falso ni descartar silenciosamente la operación.

## Monitorización de estado del circuito por integración

```java
circuitBreakerErp.getEventPublisher().onStateTransition(evento ->
    logger.warn("event=circuit_breaker.transicion sistema=erp-sap estado_anterior={} estado_nuevo={}",
        evento.getStateTransition().getFromState(), evento.getStateTransition().getToState())
);
```

Ver [`observability.md`](./observability.md) — en un sistema con múltiples integraciones, un dashboard que muestre el estado de cada Circuit Breaker individualmente (cerrado/abierto/semi-abierto) es la forma más directa de saber, de un vistazo, qué integraciones están degradadas en un momento dado, sin tener que inferirlo de logs dispersos.

## Checklist

- [ ] ¿Cada sistema externo tiene su propio Circuit Breaker, sin compartir umbrales entre integraciones de fiabilidad distinta?
- [ ] ¿Los umbrales están calibrados según el comportamiento histórico real de cada sistema, no un valor genérico copiado?
- [ ] ¿El fallback es apropiado al tipo de operación (caché para lectura, cola diferida para escritura crítica)?
- [ ] ¿Existe monitorización visible del estado de cada circuito individualmente?

Ver también: [`../../patterns/circuit-breaker.md`](../../patterns/circuit-breaker.md), [`retries-and-backoff.md`](./retries-and-backoff.md), [`observability.md`](./observability.md).

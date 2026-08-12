# Pitfalls (Integration) — Errores frecuentes

## Reintentar un error 4xx determinista

```java
// Reintentar un 404/400 no lo va a convertir en éxito
retry(() -> erpClient.obtenerPedido(idInexistente));
```

Ver [`retries-and-backoff.md`](./retries-and-backoff.md).

## Compartir un Circuit Breaker entre sistemas externos distintos

```java
CircuitBreaker cbCompartido = CircuitBreaker.of("integraciones", config); // mezcla fiabilidad de ERP + WMS + Pagos
```

Ver [`circuit-breaker.md`](./circuit-breaker.md) — un Circuit Breaker por sistema.

## Propagar el modelo/errores crudos del sistema externo

```java
throw new RuntimeException(erpResponse.getErrorCode()); // el resto del código ahora depende de códigos SAP
```

Ver [`schema-mapping.md`](./schema-mapping.md) y [`error-handling.md`](./error-handling.md).

## Diseñar una Saga sin compensaciones

```java
wmsClient.reservarStock(pedido);
pagosClient.cobrar(pedido); // si esto falla, el stock reservado nunca se libera
```

Ver [`saga.md`](./saga.md).

## Consumidor de eventos sin verificación de idempotencia

```java
@KafkaListener(topics = "pedidos.eventos")
public void consumir(PedidoEvento evento) {
    actualizarInventario(evento); // reentrega del broker duplica el efecto
}
```

Ver [`idempotency.md`](./idempotency.md).

## Sincronización batch del dataset completo en cada ejecución

```sql
SELECT * FROM productos; -- sin filtro de fecha de modificación, recarga todo cada vez
```

Ver [`data-synchronization.md`](./data-synchronization.md).

## Checklist de revisión rápida

- [ ] ¿Se reintenta algún error 4xx determinista?
- [ ] ¿Hay un Circuit Breaker compartido entre sistemas externos de fiabilidad distinta?
- [ ] ¿Se propaga el modelo de error crudo de un sistema externo sin traducir?
- [ ] ¿Existe algún paso de Saga sin su compensación correspondiente?

Ver también: [`best-practices.md`](./best-practices.md).

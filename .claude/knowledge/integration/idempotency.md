# Idempotency

> Ver [`retries-and-backoff.md`](./retries-and-backoff.md) — la idempotencia es lo que hace que reintentar una operación tras un fallo/timeout sea seguro, en lugar de arriesgar duplicar su efecto.

## Por qué es central en integración

En una llamada de red, el llamador **no siempre puede saber** si una operación tuvo éxito — un timeout puede significar que la operación falló, o que tuvo éxito pero la respuesta se perdió en el camino de vuelta. Sin idempotencia, reintentar ante esa ambigüedad arriesga ejecutar la operación dos veces (cobrar un pago dos veces, crear un pedido duplicado); con idempotencia, reintentar es siempre seguro.

## Idempotency Key: el mecanismo estándar

```
POST /api/v1/pagos
Idempotency-Key: 550e8400-e29b-41d4-a716-446655440000

{ "pedidoId": "p-123", "monto": 49.99 }
```

```java
@PostMapping("/pagos")
public ResponseEntity<PagoResponse> procesarPago(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @RequestBody PagoRequest request) {

    Optional<PagoResponse> existente = idempotencyStore.buscar(idempotencyKey);
    if (existente.isPresent()) {
        return ResponseEntity.ok(existente.get()); // misma respuesta que la primera vez, sin reejecutar
    }

    PagoResponse resultado = procesarPagoUseCase.ejecutar(request);
    idempotencyStore.guardar(idempotencyKey, resultado, ttl = Duration.ofHours(24));
    return ResponseEntity.ok(resultado);
}
```

El cliente genera una clave única (típicamente un UUID) por **intento lógico** de la operación (no por cada reintento HTTP — el mismo intento lógico reintentado usa la misma clave) — el servidor almacena el resultado asociado a esa clave durante una ventana de tiempo razonable, y ante una repetición devuelve el resultado ya calculado sin reejecutar la operación.

## Idempotencia natural vs. idempotencia forzada

```sql
-- Naturalmente idempotente: ejecutar dos veces produce el mismo estado final
UPDATE pedidos SET estado = 'CONFIRMADO' WHERE id = 'p-123';

-- NO naturalmente idempotente: cada ejecución incrementa de nuevo
UPDATE inventario SET cantidad = cantidad - 1 WHERE producto_id = 'sku-1';
```

Algunas operaciones son idempotentes por su propia naturaleza (asignar un valor absoluto); otras (incrementar/decrementar, insertar sin restricción única) requieren un mecanismo explícito — una clave de idempotencia, una restricción única en base de datos que rechace duplicados, o un identificador de negocio verificado antes de actuar (`existsByPedidoIdAndTipo`).

## Idempotencia en consumidores de eventos/colas

```java
@KafkaListener(topics = "pedidos.confirmados")
public void consumir(PedidoConfirmadoEvent evento) {
    String eventId = evento.pedidoId() + ":" + evento.version();
    if (processedEventRepository.yaProcesado(eventId)) return; // ya procesado, ignorar silenciosamente

    actualizarInventarioUseCase.ejecutar(evento.pedidoId());
    processedEventRepository.marcarComoProcesado(eventId);
}
```

Ver [`../../recipes/java-create-worker.md`](../../recipes/java-create-worker.md) para el detalle completo — la mensajería asíncrona garantiza típicamente entrega "al menos una vez" (*at-least-once*), lo que hace la idempotencia del consumidor obligatoria, no opcional, para evitar procesar el mismo evento dos veces ante una reentrega.

## TTL de las claves de idempotencia

Una clave de idempotencia no debe almacenarse indefinidamente — un TTL razonable (24-48h es habitual) equilibra la protección contra reintentos tardíos legítimos con el coste de almacenamiento indefinido de claves que ya no se van a repetir. Ver `CLAUDE.md` sección 6.5 para el patrón general de TTL en claves de caché, aplicable aquí igualmente.

## Checklist

- [ ] ¿Las operaciones de creación/modificación costosas de duplicar soportan una `Idempotency-Key`?
- [ ] ¿Se ha verificado si la operación es naturalmente idempotente, o requiere un mecanismo explícito (restricción única, clave de idempotencia)?
- [ ] ¿Los consumidores de eventos/colas verifican si el evento ya fue procesado antes de actuar?
- [ ] ¿Las claves de idempotencia almacenadas tienen TTL, no se retienen indefinidamente?

Ver también: [`retries-and-backoff.md`](./retries-and-backoff.md), [`../../recipes/java-create-worker.md`](../../recipes/java-create-worker.md), `CLAUDE.md` sección 6.5.

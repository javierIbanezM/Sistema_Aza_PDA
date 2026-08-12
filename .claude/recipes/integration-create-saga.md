# Recipe: Implementar una Saga Orquestada (Integration)

Aplica: [`../knowledge/integration/saga.md`](../knowledge/integration/saga.md), [`../knowledge/integration/idempotency.md`](../knowledge/integration/idempotency.md).

## Pasos

### 1. Identificar los pasos y sus compensaciones ANTES de escribir código

```
Paso 1: Reservar stock (WMS)         → Compensación: Liberar stock
Paso 2: Cobrar (Proveedor de pagos)  → Compensación: Reembolsar
Paso 3: Crear pedido (ERP)           → Compensación: Cancelar pedido en ERP
```

Diseñar esta tabla explícitamente con [`../agents/senior-business-analyst.md`](../agents/senior-business-analyst.md) antes de implementar — confirmar con negocio que cada compensación es aceptable como equivalente de la reversión.

### 2. Definir el estado de la Saga, persistido (para poder recuperarse ante caída del proceso)

```java
@Entity
public class SagaConfirmarPedido {
    @Id private String id;
    @Enumerated(EnumType.STRING) private EstadoSaga estado; // INICIADA, STOCK_RESERVADO, PAGO_CONFIRMADO, COMPLETADA, COMPENSANDO, FALLIDA
    private String pedidoId;
}
```

Persistir el estado permite que, si el proceso que ejecuta la Saga cae a mitad, un mecanismo de recuperación pueda retomarla desde el último paso completado, en lugar de perder el rastro de una operación a medias.

### 3. Implementar el orquestador

```java
@Component
public class ConfirmarPedidoSagaOrchestrator {

    public void ejecutar(String pedidoId) {
        SagaConfirmarPedido saga = sagaRepository.crear(pedidoId);

        try {
            wmsClient.reservarStock(pedidoId);
            saga.transicionar(EstadoSaga.STOCK_RESERVADO);

            pagosClient.cobrar(pedidoId);
            saga.transicionar(EstadoSaga.PAGO_CONFIRMADO);

            erpClient.crearPedido(pedidoId);
            saga.transicionar(EstadoSaga.COMPLETADA);

        } catch (StockNoDisponibleException e) {
            saga.transicionar(EstadoSaga.FALLIDA);
        } catch (PagoFallidoException e) {
            wmsClient.liberarStock(pedidoId); // compensación
            saga.transicionar(EstadoSaga.FALLIDA);
        } catch (ErpFallidoException e) {
            pagosClient.reembolsar(pedidoId);  // compensación en orden inverso
            wmsClient.liberarStock(pedidoId);
            saga.transicionar(EstadoSaga.FALLIDA);
        } finally {
            sagaRepository.guardar(saga);
        }
    }
}
```

### 4. Cada paso y cada compensación deben ser idempotentes

```java
public void liberarStock(String pedidoId) {
    if (yaLiberado(pedidoId)) return; // idempotente: seguro reintentar si la saga se recupera tras una caída
    wmsClient.liberarStockReal(pedidoId);
    marcarLiberado(pedidoId);
}
```

Ver [`../knowledge/integration/idempotency.md`](../knowledge/integration/idempotency.md) — dado que un mecanismo de recuperación puede reintentar pasos ya ejecutados parcialmente, tanto los pasos originales como sus compensaciones deben tolerar reejecución sin duplicar efectos.

### 5. Mecanismo de recuperación para sagas interrumpidas

```java
@Scheduled(fixedDelay = 60000)
public void recuperarSagasIncompletas() {
    List<SagaConfirmarPedido> incompletas = sagaRepository.buscarEnProgresoDesdeHace(Duration.ofMinutes(5));
    incompletas.forEach(this::reanudarDesdeUltimoEstado);
}
```

### 6. Test de cada camino de compensación

```java
@Test
void debeLiberarStockSiElPagoFalla() {
    when(pagosClient.cobrar(any())).thenThrow(new PagoFallidoException());

    orchestrator.ejecutar(pedidoId);

    verify(wmsClient).liberarStock(pedidoId);
    assertThat(sagaRepository.buscar(pedidoId).getEstado()).isEqualTo(EstadoSaga.FALLIDA);
}
```

## Checklist final

- [ ] ¿Cada paso tiene su compensación diseñada y confirmada con negocio antes de implementar?
- [ ] ¿El estado de la Saga está persistido, permitiendo recuperación ante caída del proceso?
- [ ] ¿Tanto los pasos como las compensaciones son idempotentes?
- [ ] ¿Existe un mecanismo de recuperación para sagas interrumpidas?
- [ ] Ejecutar [`../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`../knowledge/integration/saga.md`](../knowledge/integration/saga.md), [`integration-create-resilient-client.md`](./integration-create-resilient-client.md).

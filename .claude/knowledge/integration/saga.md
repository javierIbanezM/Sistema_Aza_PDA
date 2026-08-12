# Saga

> Ver `CLAUDE.md` sección 17 (Microservicios) donde el patrón Saga se menciona como estándar del framework para consistencia entre servicios. Este documento lo desarrolla en profundidad, con foco en integración entre sistemas heterogéneos (no solo microservicios propios).

## El problema: transacciones que cruzan sistemas sin transacción distribuida

Confirmar un pedido puede requerir: reservar stock en el WMS, cobrar en el proveedor de pagos, y crear el pedido en el ERP — tres sistemas distintos, cada uno con su propia base de datos, sin una transacción ACID que abarque los tres. Si el paso 2 falla tras completar el paso 1, el sistema queda en un estado inconsistente salvo que exista un mecanismo explícito para revertir el paso 1.

## Saga coreografiada: cada servicio reacciona a eventos

```
PedidoService: publica PedidoCreado
        ↓
WmsService: reserva stock, publica StockReservado (o StockNoDisponible)
        ↓
PagosService: cobra, publica PagoConfirmado (o PagoFallido)
        ↓
Si PagoFallido: WmsService escucha y libera el stock reservado (compensación)
```

Cada servicio publica eventos de su propio resultado y escucha los eventos relevantes de otros para decidir su siguiente acción (incluidas las compensaciones) — sin un orquestador central. Ver [`event-driven.md`](./event-driven.md) para el mecanismo de publicación/suscripción subyacente.

**Ventaja**: totalmente desacoplado, cada servicio solo conoce los eventos, no a los demás servicios directamente.
**Desventaja**: el flujo completo de negocio no está descrito en ningún lugar único — entender "qué pasa cuando se confirma un pedido" requiere rastrear los listeners de cada servicio individualmente.

## Saga orquestada: un coordinador explícito dirige el flujo

```java
public class ConfirmarPedidoSaga {
    public void ejecutar(Pedido pedido) {
        try {
            wmsClient.reservarStock(pedido);
            pagosClient.cobrar(pedido);
            erpClient.crearPedido(pedido);
        } catch (StockNoDisponibleException e) {
            // nada que compensar, el primer paso falló
            marcarPedidoFallido(pedido, e);
        } catch (PagoFallidoException e) {
            wmsClient.liberarStock(pedido); // compensación del paso anterior
            marcarPedidoFallido(pedido, e);
        } catch (ErpFallidoException e) {
            pagosClient.reembolsar(pedido);   // compensación en orden inverso
            wmsClient.liberarStock(pedido);
            marcarPedidoFallido(pedido, e);
        }
    }
}
```

**Ventaja**: el flujo de negocio completo está descrito en un único lugar, fácil de entender y modificar.
**Desventaja**: el orquestador conoce a todos los servicios participantes, mayor acoplamiento que la variante coreografiada.

## Cuándo elegir cada variante

- **Coreografiada**: pocos pasos, servicios que ya se comunican por eventos de forma natural, equipos independientes que prefieren no depender de un coordinador central.
- **Orquestada**: flujos complejos con muchos pasos y compensaciones, donde la claridad de tener el proceso completo descrito en un único lugar supera el coste de acoplamiento — preferible en la mayoría de integraciones con sistemas externos heterogéneos (ERPs, WMS), donde ya existe suficiente complejidad de por sí sin añadir la dificultad de rastrear un flujo distribuido en múltiples listeners.

## Compensaciones: el paso más fácil de olvidar

Cada paso de una Saga que tiene efectos reales necesita su **acción de compensación** correspondiente, diseñada **al mismo tiempo** que el paso original, no como una idea de último momento — "reservar stock" necesita "liberar stock"; "cobrar" necesita "reembolsar". Una compensación no siempre es una reversión perfecta (un reembolso no es idéntico a "nunca haber cobrado", puede tener implicaciones contables/fiscales distintas) — esto debe ser una decisión de negocio explícita, no una asunción técnica (ver [`../../agents/senior-business-analyst.md`](../../agents/senior-business-analyst.md)).

## Checklist

- [ ] ¿Se ha elegido coreografiada u orquestada según la complejidad del flujo y el número de servicios involucrados?
- [ ] ¿Cada paso con efecto real tiene su acción de compensación diseñada explícitamente, no improvisada tras el hecho?
- [ ] ¿Las compensaciones se ejecutan en orden inverso a los pasos originales?
- [ ] ¿Se ha confirmado con negocio que una compensación (ej. reembolso) es aceptable como equivalente a la reversión, no solo asumido técnicamente?

Ver también: [`event-driven.md`](./event-driven.md), `CLAUDE.md` sección 17, [`data-synchronization.md`](./data-synchronization.md).

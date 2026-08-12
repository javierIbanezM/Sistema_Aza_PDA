# Pattern: Facade

## Descripción

Facade proporciona una interfaz simple y unificada sobre un subsistema complejo compuesto por varias clases/servicios que colaboran entre sí, sin exponer al código cliente los detalles internos de esa colaboración.

## Cuándo usarlo

- Cuando un caso de uso requiere orquestar varios servicios/componentes de infraestructura en un orden concreto (ej. procesar un pedido implica validar stock, cobrar el pago y notificar al almacén), y se quiere ofrecer al código cliente una única operación en lugar de exigirle conocer y orquestar los tres pasos.
- Al integrar con un subsistema de terceros complejo (una librería con muchas clases de configuración) exponiendo solo las operaciones que el proyecto realmente necesita.
- Para reducir el acoplamiento entre capas: la capa de presentación llama a una Facade de la capa de aplicación, sin conocer los servicios de dominio/infraestructura internos que esa Facade orquesta.

## Cuándo NO usarlo

- Cuando el subsistema ya es simple (una o dos clases) — envolver algo simple en una Facade añade indirección sin beneficio (ver KISS en [`../knowledge/architecture/dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Como sustituto de un buen diseño de casos de uso en Clean Architecture — en ese contexto, el propio caso de uso (`CrearPedidoUseCase`) ya cumple el rol de Facade de forma natural; no es necesario añadir una capa Facade adicional por encima.

## Ventajas

- Reduce el acoplamiento del código cliente con los detalles internos de un subsistema.
- Simplifica el uso de APIs/librerías complejas, exponiendo solo lo necesario.
- Facilita el testing del código cliente, que solo necesita un doble de test de la Facade, no de cada componente interno.

## Desventajas

- Puede convertirse en un [God Object](../anti-patterns/god-object.md) si acumula demasiadas responsabilidades no relacionadas en lugar de limitarse a orquestar un subsistema cohesivo.
- Oculta la complejidad interna, lo cual es positivo para el consumidor pero puede dificultar el diagnóstico de problemas si la Facade no propaga suficiente contexto en sus errores.

## Ejemplo

```java
// Subsistema complejo: varios servicios de infraestructura que colaboran
public class ValidadorStock { public void validar(Pedido pedido) { /* ... */ } }
public class ProcesadorPagos { public ResultadoPago cobrar(Pedido pedido) { /* ... */ } }
public class NotificadorAlmacen { public void notificar(Pedido pedido) { /* ... */ } }
public class NotificadorCliente { public void confirmar(Pedido pedido) { /* ... */ } }

// Facade: expone una única operación simple, orquestando el subsistema internamente
public class ProcesarPedidoFacade {
    private final ValidadorStock validadorStock;
    private final ProcesadorPagos procesadorPagos;
    private final NotificadorAlmacen notificadorAlmacen;
    private final NotificadorCliente notificadorCliente;

    public ProcesarPedidoFacade(ValidadorStock v, ProcesadorPagos p, NotificadorAlmacen na, NotificadorCliente nc) {
        this.validadorStock = v;
        this.procesadorPagos = p;
        this.notificadorAlmacen = na;
        this.notificadorCliente = nc;
    }

    public ResultadoPedido procesar(Pedido pedido) {
        validadorStock.validar(pedido);
        ResultadoPago pago = procesadorPagos.cobrar(pedido);
        if (!pago.isExitoso()) {
            return ResultadoPedido.fallido(pago.getMotivo());
        }
        notificadorAlmacen.notificar(pedido);
        notificadorCliente.confirmar(pedido);
        return ResultadoPedido.exitoso(pedido.getId());
    }
}

// Código cliente: una única llamada, sin conocer los cuatro servicios internos
ResultadoPedido resultado = procesarPedidoFacade.procesar(pedido);
```

## Errores comunes

- Convertir la Facade en un [God Object](../anti-patterns/god-object.md), añadiendo operaciones no relacionadas con el subsistema original que orquesta.
- Duplicar lógica de orquestación tanto en la Facade como en el código cliente que la usa, permitiendo "saltarse" la Facade y llamar directamente a los componentes internos en algunos lugares — rompe el propósito de encapsulación del patrón.

Ver también: [`adapter.md`](./adapter.md), [`../anti-patterns/god-object.md`](../anti-patterns/god-object.md), [`../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md).

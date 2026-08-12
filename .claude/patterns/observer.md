# Patrón: Observer

## Descripción

Observer define una dependencia uno-a-muchos entre objetos, de forma que cuando el objeto observado ("sujeto"/"publisher") cambia de estado, todos sus observadores ("subscribers") son notificados automáticamente, sin que el sujeto necesite conocer los detalles concretos de cada observador. Es la base conceptual de los Domain Events (ver [`../ddd.md`](../knowledge/architecture/ddd.md)) y de los sistemas de mensajería basados en eventos (ver la Skill de Kafka/RabbitMQ).

## Cuándo usarlo

- Cuando varias partes del sistema necesitan reaccionar a un cambio de estado sin acoplarse directamente entre sí (ej. al confirmar un pedido, el sistema de notificaciones, el de facturación y el de analítica deben reaccionar, sin que `ServicioPedidos` conozca a ninguno de ellos directamente).
- Cuando se quiere añadir nuevos "reaccionadores" a un evento sin modificar el código que lo emite (cumple OCP, ver [`../solid.md`](../knowledge/architecture/solid.md)).
- Como base de UI reactiva (`Flow`/`StateFlow` en Kotlin, `Observable` en RxJS, `useState`/`useEffect` en React son variantes de este patrón aplicadas a la actualización de interfaces).

## Cuándo NO usarlo

- Cuando la reacción a un cambio es única, simple y no va a crecer — una llamada directa a un método es más simple de seguir que registrar un observador (ver KISS en [`../dry-kiss-yagni.md`](../knowledge/architecture/dry-kiss-yagni.md)).
- Cuando el orden de ejecución entre observadores importa y no hay un mecanismo claro de garantizarlo — Observer no garantiza orden por defecto, lo que puede ser una fuente de bugs sutiles si el negocio sí requiere una secuencia específica.

## Ventajas

- Desacopla al emisor de los consumidores de un evento; se pueden añadir/quitar observadores sin tocar el código del sujeto.
- Facilita la extensión del sistema con nuevas reacciones a eventos existentes sin riesgo de romper el flujo principal.
- Es la base natural para arquitecturas orientadas a eventos y para propagar Domain Events entre Bounded Contexts (ver [`../ddd.md`](../knowledge/architecture/ddd.md)).

## Desventajas

- Puede dificultar seguir el flujo completo de una operación ("¿qué pasa realmente cuando se confirma un pedido?" requiere buscar todos los observadores registrados, no solo leer un único método secuencial).
- Sin cuidado, los observadores pueden introducir efectos secundarios ocultos, acoplamiento temporal implícito (un observador que depende de que otro se ejecute antes) o fallos silenciosos si una excepción en un observador no se maneja explícitamente.

## Ejemplo

### Implementación básica (in-process, síncrona)

```typescript
type Handler<T> = (evento: T) => void;

class EventBus {
  private handlers = new Map<string, Handler<any>[]>();

  suscribir<T>(tipoEvento: string, handler: Handler<T>): void {
    const lista = this.handlers.get(tipoEvento) ?? [];
    lista.push(handler);
    this.handlers.set(tipoEvento, lista);
  }

  publicar<T>(tipoEvento: string, evento: T): void {
    const lista = this.handlers.get(tipoEvento) ?? [];
    for (const handler of lista) {
      try {
        handler(evento);
      } catch (error) {
        // Un observador que falla no debe romper a los demás ni al emisor
        logger.error(`Error en handler de ${tipoEvento}`, error);
      }
    }
  }
}

// Registro de observadores (en el arranque de la aplicación)
eventBus.suscribir('pedido.confirmado', (evento: PedidoConfirmadoEvent) => {
  servicioNotificaciones.notificarCliente(evento.pedidoId);
});

eventBus.suscribir('pedido.confirmado', (evento: PedidoConfirmadoEvent) => {
  servicioFacturacion.generarFactura(evento.pedidoId);
});

// Emisión del evento (el sujeto no conoce a ninguno de los dos observadores)
eventBus.publicar('pedido.confirmado', new PedidoConfirmadoEvent(pedido.id));
```

### Observer asíncrono/distribuido (a través de una cola de mensajería)

Cuando los observadores viven en servicios distintos (arquitectura de microservicios, ver `CLAUDE.md` sección 17), el patrón Observer se implementa mediante una cola/broker de mensajería (Kafka, RabbitMQ) en lugar de un `EventBus` in-process, lo que además provee persistencia del evento, reintentos y desacoplamiento temporal (el observador puede procesar el evento más tarde, incluso si estaba caído en el momento de la publicación). Ver la Skill de mensajería correspondiente para el detalle de implementación.

## Errores comunes

- No manejar excepciones dentro de cada observador, permitiendo que el fallo de uno impida la ejecución de los demás o incluso del propio sujeto emisor.
- Depender implícitamente del orden de registro de los observadores para garantizar una secuencia de negocio — si el orden importa, modelarlo explícitamente (ej. un pipeline secuencial) en lugar de confiar en el orden de suscripción.
- Fugas de memoria por no des-suscribir observadores cuando el objeto que los registró deja de existir (especialmente relevante en UI: listeners de eventos de UI no removidos al destruir un componente — ver `anti-patterns/MemoryLeak.md` en la Skill de la plataforma correspondiente).

Ver también: [`../ddd.md`](../knowledge/architecture/ddd.md) (Domain Events), [`Strategy.md`](./strategy.md), sección 16 de `CLAUDE.md` (Kafka/RabbitMQ).

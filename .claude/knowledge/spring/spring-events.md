# Spring Events

> Ver [`../../patterns/observer.md`](../../patterns/observer.md) para el patrón de diseño que este mecanismo implementa, y [`../architecture/ddd.md`](../architecture/ddd.md) para el concepto de Domain Event que normalmente se transporta con este mecanismo dentro de un mismo proceso.

## Publicar un evento

```java
public record PedidoConfirmadoEvent(String pedidoId, Instant ocurridoEn) {}
```

```java
@Service
public class ConfirmarPedidoUseCase {

    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void ejecutar(PedidoId id) {
        Pedido pedido = pedidoRepository.buscarPorId(id).orElseThrow();
        pedido.confirmar();
        pedidoRepository.guardar(pedido);

        eventPublisher.publishEvent(new PedidoConfirmadoEvent(pedido.getId().valor(), Instant.now()));
    }
}
```

## Consumir un evento

```java
@Component
public class NotificarClienteAlConfirmarListener {

    @EventListener
    public void onPedidoConfirmado(PedidoConfirmadoEvent evento) {
        notificadorEmail.notificarConfirmacion(evento.pedidoId());
    }
}
```

Este desacoplamiento evita que `ConfirmarPedidoUseCase` conozca directamente a `NotificadorEmail`, `ServicioFacturacion`, `ServicioInventario` — cada listener reacciona de forma independiente, y añadir un nuevo listener no requiere modificar el caso de uso emisor (ver [`../../anti-patterns/god-object.md`](../../anti-patterns/god-object.md)).

## `@TransactionalEventListener`: publicar solo tras commit exitoso

```java
@Component
public class NotificarClienteAlConfirmarListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPedidoConfirmado(PedidoConfirmadoEvent evento) {
        // se ejecuta SOLO si la transacción que publicó el evento hizo commit exitosamente
        notificadorEmail.notificarConfirmacion(evento.pedidoId());
    }
}
```

Con `@EventListener` simple, el listener se ejecuta de forma síncrona dentro de la misma transacción que lo publicó — si la transacción hace rollback después de publicar el evento, el efecto secundario (email ya enviado) ya ocurrió y no se puede deshacer. `@TransactionalEventListener(phase = AFTER_COMMIT)` evita este problema, ejecutando el listener solo tras confirmar que la transacción se completó con éxito.

## Procesamiento asíncrono

```java
@Configuration
@EnableAsync
public class AsyncConfig {
    @Bean
    public Executor eventTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setThreadNamePrefix("event-listener-");
        executor.initialize();
        return executor;
    }
}
```

```java
@Async("eventTaskExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onPedidoConfirmado(PedidoConfirmadoEvent evento) {
    notificadorEmail.notificarConfirmacion(evento.pedidoId()); // no bloquea la respuesta HTTP al cliente
}
```

## Eventos in-process vs. eventos distribuidos (Kafka/RabbitMQ)

Spring Events es **in-process**: solo llega a listeners dentro de la misma instancia de la aplicación. Para comunicar el evento a **otros servicios** (arquitectura de microservicios, ver `CLAUDE.md` sección 17), el evento debe publicarse además en una cola/broker real (Kafka/RabbitMQ) — un patrón habitual es que un listener de Spring Events transaccional (`AFTER_COMMIT`) sea el responsable de publicar el evento hacia el broker externo, garantizando que solo se publica externamente si la transacción local tuvo éxito.

## Checklist

- [ ] ¿Los efectos secundarios que no deben revertirse con un rollback usan `@TransactionalEventListener(phase = AFTER_COMMIT)`, no `@EventListener` simple?
- [ ] ¿Los listeners con I/O costoso (envío de email, llamada externa) son asíncronos, para no bloquear la respuesta al cliente?
- [ ] ¿Se ha evitado confundir Spring Events (in-process) con un mecanismo de mensajería distribuida entre servicios?

Ver también: [`../../patterns/observer.md`](../../patterns/observer.md), [`../architecture/ddd.md`](../architecture/ddd.md), `CLAUDE.md` sección 16 (Kafka/RabbitMQ) y sección 17 (Microservicios).

# Recipe: Crear un Worker / Consumer Asíncrono (Java/Spring)

Un "worker" procesa trabajo fuera del ciclo petición-respuesta HTTP: consumidor de una cola (Kafka/RabbitMQ), listener de eventos internos, o tarea programada. Aplica: [`../../standards/exceptions.md`](../standards/exceptions.md) (errores transitorios vs. permanentes), [`../../patterns/circuit-breaker.md`](../patterns/circuit-breaker.md), `CLAUDE.md` sección 16.

## Pasos (consumidor Kafka — adaptar para RabbitMQ según `@RabbitListener`)

### 1. Definir el evento que se consume (contrato versionado)

```java
public record PedidoConfirmadoEventDto(
    String pedidoId,
    String clienteId,
    Instant ocurridoEn,
    int version // versionado del esquema del evento, ver CLAUDE.md sección 16.2
) {}
```

### 2. Escribir el consumidor, idempotente por diseño

```java
@Component
public class PedidoConfirmadoConsumer {

    private final ActualizarInventarioUseCase actualizarInventarioUseCase;
    private final ProcessedEventRepository processedEventRepository; // para garantizar idempotencia

    public PedidoConfirmadoConsumer(ActualizarInventarioUseCase useCase, ProcessedEventRepository repo) {
        this.actualizarInventarioUseCase = useCase;
        this.processedEventRepository = repo;
    }

    @KafkaListener(topics = "pedidos.confirmados", groupId = "inventario-service")
    public void consumir(PedidoConfirmadoEventDto evento, Acknowledgment ack) {
        String eventId = evento.pedidoId() + ":" + evento.version();

        if (processedEventRepository.yaProcesado(eventId)) {
            log.info("Evento {} ya procesado, ignorando (idempotencia)", eventId);
            ack.acknowledge();
            return;
        }

        try {
            actualizarInventarioUseCase.ejecutar(evento.pedidoId());
            processedEventRepository.marcarComoProcesado(eventId);
            ack.acknowledge(); // ack manual, no automático — ver CLAUDE.md sección 16.1
        } catch (ErrorTransitorioException e) {
            log.warn("Error transitorio procesando evento {}, se reintentará", eventId, e);
            // no se hace ack: el broker reentregará según la configuración de reintentos
        } catch (Exception e) {
            log.error("Error no recuperable procesando evento {}, enviando a DLQ", eventId, e);
            ack.acknowledge(); // se saca de la cola principal; el error handler del listener lo enruta a DLQ
        }
    }
}
```

**Idempotencia obligatoria**: al menos-una-vez ("at-least-once") es el estándar por defecto en colas — el consumidor **debe** tolerar reentrega del mismo mensaje sin duplicar efectos (ver [`../../standards/exceptions.md`](../standards/exceptions.md), distinción entre errores transitorios y permanentes).

### 3. Configurar Dead Letter Queue para mensajes que fallan repetidamente

```yaml
spring:
  kafka:
    consumer:
      group-id: inventario-service
      properties:
        max.poll.interval.ms: 300000
```

```java
@Bean
public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
    var recoverer = new DeadLetterPublishingRecoverer(template);
    var backOff = new ExponentialBackOff(1000L, 2.0); // backoff exponencial entre reintentos
    backOff.setMaxElapsedTime(30000L);
    return new DefaultErrorHandler(recoverer, backOff);
}
```

Ver `CLAUDE.md` sección 16 para el razonamiento completo de DLQ y reintentos.

### 4. Tarea programada (alternativa: `@Scheduled` para jobs periódicos, no basados en eventos)

```java
@Component
public class LimpiezaPedidosExpiradosJob {

    private final PedidoRepository pedidoRepository;

    @Scheduled(cron = "0 0 3 * * *") // 3:00 AM diario, configurado externamente idealmente
    public void ejecutar() {
        log.info("Iniciando limpieza de pedidos expirados");
        int cantidad = pedidoRepository.cancelarPedidosExpirados();
        log.info("Limpieza completada: {} pedidos cancelados", cantidad);
    }
}
```

Ver [`java-create-scheduler.md`](./java-create-scheduler.md) para el detalle completo de tareas programadas.

### 5. Tests

```java
@Test
void deberiaIgnorarUnEventoYaProcesado() {
    when(processedEventRepository.yaProcesado(anyString())).thenReturn(true);

    consumer.consumir(evento, ack);

    verify(actualizarInventarioUseCase, never()).ejecutar(any());
    verify(ack).acknowledge();
}

@Test
void noDeberiaHacerAckSiElErrorEsTransitorio() {
    doThrow(new ErrorTransitorioException("timeout")).when(actualizarInventarioUseCase).ejecutar(any());

    consumer.consumir(evento, ack);

    verify(ack, never()).acknowledge(); // permite reentrega
}
```

## Checklist final

- [ ] ¿El consumidor es idempotente (tolera reentrega del mismo mensaje sin duplicar efectos)?
- [ ] ¿Se distingue entre errores transitorios (sin ack, permite reintento) y permanentes (ack + DLQ)?
- [ ] ¿Existe una Dead Letter Queue configurada con backoff exponencial?
- [ ] Ejecutar [`../../checklists/architecture.md`](../checklists/architecture.md).

Ver también: [`java-create-scheduler.md`](./java-create-scheduler.md), `CLAUDE.md` sección 16.

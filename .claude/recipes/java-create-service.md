# Recipe: Crear un Service / Caso de Uso (Java/Spring)

Aplica: [`../../knowledge/architecture/clean-architecture.md`](../knowledge/architecture/clean-architecture.md), [`../../knowledge/spring/spring-core.md`](../knowledge/spring/spring-core.md), [`../../standards/exceptions.md`](../standards/exceptions.md).

Un "Service" en este framework es un **caso de uso** de la capa de aplicación: orquesta el dominio, no contiene reglas de negocio propias (esas viven en las entidades/value objects de `domain/`, ver [`../../knowledge/architecture/ddd.md`](../knowledge/architecture/ddd.md)).

## Pasos

### 1. Definir el puerto que el caso de uso necesita (si no existe ya)

```java
public interface PedidoRepository {
    void guardar(Pedido pedido);
    Optional<Pedido> buscarPorId(PedidoId id);
}
```

### 2. Definir excepciones de dominio específicas (si no existen ya)

```java
public class PedidoNoEncontradoException extends DomainException {
    public PedidoNoEncontradoException(PedidoId id) {
        super("Pedido no encontrado: " + id.valor());
    }
}
```

Ver [`../../standards/exceptions.md`](../standards/exceptions.md) — extiende una base `DomainException` propia del proyecto, nunca `RuntimeException` directamente sin tipar.

### 3. Escribir el caso de uso: una única responsabilidad, un único método público

```java
@Service
public class ConfirmarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final ApplicationEventPublisher eventPublisher; // para desacoplar efectos secundarios, ver patterns/observer.md

    public ConfirmarPedidoUseCase(PedidoRepository pedidoRepository, ApplicationEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void ejecutar(PedidoId id) {
        Pedido pedido = pedidoRepository.buscarPorId(id)
            .orElseThrow(() -> new PedidoNoEncontradoException(id));

        pedido.confirmar(); // la invariante ("no confirmar sin líneas") vive en la entidad, no aquí

        pedidoRepository.guardar(pedido);
        eventPublisher.publishEvent(new PedidoConfirmadoEvent(pedido.getId()));
    }
}
```

### 4. Reaccionar al evento en otros casos de uso, en lugar de llamarlos directamente

```java
@Component
public class NotificarClienteAlConfirmarListener {

    private final NotificadorEmail notificadorEmail;

    @EventListener
    @Async // opcional, según si la notificación debe ser síncrona con la transacción o no
    public void onPedidoConfirmado(PedidoConfirmadoEvent evento) {
        notificadorEmail.notificarConfirmacion(evento.pedidoId());
    }
}
```

Este desacoplamiento evita que `ConfirmarPedidoUseCase` se convierta en un [God Object](../anti-patterns/god-object.md) a medida que se añaden más efectos secundarios (facturación, actualización de inventario) — ver [`../../patterns/observer.md`](../patterns/observer.md).

### 5. Tests unitarios (sin Spring, sin base de datos)

```java
@ExtendWith(MockitoExtension.class)
class ConfirmarPedidoUseCaseTest {

    @Mock private PedidoRepository pedidoRepository;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ConfirmarPedidoUseCase useCase;

    @Test
    void deberiaLanzarExcepcionSiElPedidoNoExiste() {
        when(pedidoRepository.buscarPorId(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.ejecutar(new PedidoId("p-999")))
            .isInstanceOf(PedidoNoEncontradoException.class);
    }

    @Test
    void deberiaPublicarElEventoTrasConfirmar() {
        Pedido pedido = new PedidoTestBuilder().conLineas(List.of(unaLinea())).build();
        when(pedidoRepository.buscarPorId(pedido.getId())).thenReturn(Optional.of(pedido));

        useCase.ejecutar(pedido.getId());

        verify(eventPublisher).publishEvent(any(PedidoConfirmadoEvent.class));
    }
}
```

Ver [`../../knowledge/junit/junit.md`](../knowledge/junit/junit.md) y [`../../knowledge/mockito/mockito.md`](../knowledge/mockito/mockito.md).

## Checklist final

- [ ] ¿El caso de uso tiene un único método público con un único propósito?
- [ ] ¿Las invariantes de negocio viven en el dominio, no en el caso de uso?
- [ ] ¿Los efectos secundarios (notificar, facturar) están desacoplados vía eventos, no encadenados directamente?
- [ ] Ejecutar [`../../checklists/architecture.md`](../checklists/architecture.md) completo.

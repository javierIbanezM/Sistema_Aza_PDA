// snippets/java/service.java
// Caso de uso de aplicación — ver knowledge/spring/spring-core.md y knowledge/architecture/clean-architecture.md.

@Service
public class ConfirmarPedidoUseCase {

    private final PedidoRepository pedidoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public ConfirmarPedidoUseCase(PedidoRepository pedidoRepository, ApplicationEventPublisher eventPublisher) {
        this.pedidoRepository = pedidoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void ejecutar(PedidoId id) {
        Pedido pedido = pedidoRepository.buscarPorId(id)
            .orElseThrow(() -> new PedidoNoEncontradoException(id));

        pedido.confirmar(); // invariante de negocio vive en la entidad, no aquí

        pedidoRepository.guardar(pedido);
        eventPublisher.publishEvent(new PedidoConfirmadoEvent(pedido.getId()));
    }
}

// Listener desacoplado, reacciona al evento sin que el caso de uso lo conozca directamente
@Component
public class NotificarClienteAlConfirmarListener {

    private final NotificadorEmail notificadorEmail;

    public NotificarClienteAlConfirmarListener(NotificadorEmail notificadorEmail) {
        this.notificadorEmail = notificadorEmail;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPedidoConfirmado(PedidoConfirmadoEvent evento) {
        notificadorEmail.notificarConfirmacion(evento.pedidoId());
    }
}

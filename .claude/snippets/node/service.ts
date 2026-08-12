// snippets/node/service.ts
// Caso de uso con eventos desacoplados — ver recipes/node-create-service.md.

@Injectable()
export class ConfirmarPedidoUseCase {
  constructor(
    @Inject(PEDIDO_REPOSITORY) private readonly pedidoRepository: PedidoRepository,
    private readonly eventBus: EventBus,
  ) {}

  async ejecutar(id: string): Promise<void> {
    const pedido = await this.pedidoRepository.buscarPorId(id);
    if (!pedido) throw new PedidoNoEncontradoException(id);

    pedido.confirmar();

    await this.pedidoRepository.guardar(pedido);
    this.eventBus.publish(new PedidoConfirmadoEvent(pedido.id));
  }
}

@Injectable()
export class NotificarClienteAlConfirmarHandler {
  constructor(private readonly notificadorEmail: NotificadorEmail) {}

  async handle(evento: PedidoConfirmadoEvent): Promise<void> {
    await this.notificadorEmail.notificarConfirmacion(evento.pedidoId);
  }
}
